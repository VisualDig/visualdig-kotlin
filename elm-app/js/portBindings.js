export default function PortBindings(elm, pageQuerier, searchService) {
    this.elm = elm;
    this.pageQuerier = pageQuerier;
    this.searchService = searchService;

    this.elmClickSearch =
        function (clickAction) {
            var node = this.pageQuerier.getElementByDigId(clickAction.digId);
            if (node) {
                this.pageQuerier.clickPageNode(node);
                this.elm.ports.click_searchResult.send({
                    result: "Success",
                    message: ""
                })
            } else {
                // execute another search using text query from before
                var searchResult = this.pageQuerier.findTextSearch(clickAction.textQuery);
                if (searchResult.found) {
                    this.pageQuerier.clickPageNode(searchResult.found);
                    searchResult.found.dataset.digId = clickAction.digId;
                    this.elm.ports.click_searchResult.send({
                        result: "Success",
                        message: ""
                    })
                } else {
                    this.elm.ports.click_searchResult.send({
                        result: "Failure_QueryExpired",
                        message: "Text element not visible yet"
                    })
                }
            }
        };


    this.elmFindTextSearch =
        function (text) {
            var textResult = this.searchService.textSearch(text, text);
            if (textResult.found) {
                this.elm.ports.findText_searchResult.send({
                    result: "Success",
                    digId: textResult.digId,
                    htmlId: textResult.found.id,
                    closestMatches: []
                });
            } else {
                this.elm.ports.findText_searchResult.send({
                    result: "Failure_" + textResult.result,
                    digId: null,
                    htmlId: null,
                    closestMatches: textResult.closestMatches
                });
            }
        };

    this.elmSpacialSearch =
        function (spacialSearchAction) {
            var node = this.pageQuerier.getElementByDigId(spacialSearchAction.digId);
            if (node) {
                var result = this.searchService.spacialSearchFromAnchor(node, spacialSearchAction);
                if (result.found) {
                    this.elm.ports.spacialSearch_result.send({
                        result: "Success",
                        digId: result.digId,
                        htmlId: result.found.id,
                        closeResults: []
                    });
                } else {
                    this.elm.ports.spacialSearch_result.send({
                        result: "Failure_" + result.result,
                        digId: null,
                        htmlId: null,
                        closeResults: result.closeResults
                    });
                }
            } else {
                // Original anchor element was removed from DOM somehow.
                // Eventually we'll walk through all prev queries in order
                // to get the anchor node. Right now we'll just go 1 level deep.
                var latestQuery = spacialSearchAction.prevQueries[0];
                if (latestQuery.queryType == "TextQuery") {
                    var textResult = this.pageQuerier.findTextSearch(latestQuery.textQuery.text);
                    if (textResult.found) {
                        var result = this.searchService.spacialSearchFromAnchor(textResult.found, spacialSearchAction);
                        if (result.found) {
                            this.elm.ports.spacialSearch_result.send({
                                result: "Success",
                                digId: result.digId,
                                htmlId: result.found.id,
                                closeResults: []
                            });
                        } else {
                            this.elm.ports.spacialSearch_result.send({
                                result: "Failure_" + result.result,
                                digId: null,
                                htmlId: null,
                                closeResults: result.closeResults
                            });
                        }
                    } else {
                        this.elm.ports.spacialSearch_result.send({
                            result: "Failure_QueryExpired",
                            digId: null,
                            htmlId: null,
                            closeResults: []
                        });
                    }
                }
            }
        };


    // Hook into all elm events below
    this.elm.ports.findText_search.subscribe(this.elmFindTextSearch.bind(this));
    this.elm.ports.click_searchText.subscribe(this.elmClickSearch.bind(this));
    this.elm.ports.spacialSearch.subscribe(this.elmSpacialSearch.bind(this));
}
