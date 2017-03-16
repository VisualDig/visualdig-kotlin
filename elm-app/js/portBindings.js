
export function PortBindings(elm, pageQuerier) {
    this.elm = elm;
    this.nextDigId = 0;
    this.pageQuerier = pageQuerier;

    this.elmClickSearch =
        function(clickAction) {
            var node = this.pageQuerier.getElementByDigId(clickAction.digId);
            if(node) {
                this.pageQuerier.clickPageNode(node);
                this.elm.ports.click_searchResult.send({result: "Success",
                                                message: ""})
            } else {
                console.log('Couldnt find it element to click on! ', clickAction);
                // execute another search using text query from before
                this.elm.ports.click_searchResult.send({result: "Failure", message: "Text element not visible yet"})
            }
         };


    this.elmFindTextSearch =
        function(text) {
            var searchResult = this.pageQuerier.findTextSearch(text);
            if(searchResult.found) {
                var digId = this.nextDigId;
                this.nextDigId += 1;
                searchResult.found.dataset.digId = digId;
                this.elm.ports.findText_searchResult.send({result: "Success",
                                                      digId: digId,
                                                      closestMatches: []})
            } else {
                searchResult.allStrings.sort(function(a,b) {return a.distance - b.distance;});
                this.elm.ports.findText_searchResult.send({result: "Failure",
                                                      digId: null,
                                                      closestMatches: searchResult.allStrings.map(function(a) {return a.term})});
            }
        };

    // Hook into all elm events below
    this.elm.ports.findText_search.subscribe(this.elmFindTextSearch.bind(this));
    this.elm.ports.click_searchText.subscribe(this.elmClickSearch.bind(this));
}
