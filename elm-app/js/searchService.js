export default function SearchService(pageQuerier, startDigId) {
    this.pageQuerier = pageQuerier;
    this.nextDigId = startDigId || 0;
}


SearchService.prototype.issueDigId = function () {
    var digId = this.nextDigId;
    this.nextDigId += 1;
    return digId;
};

SearchService.prototype.removePrevQueries = function (action) {
    var freshAction = action;
    if (action.prevQueries !== undefined) {
        freshAction = Object.assign({}, action);
        delete freshAction.prevQueries;
    }
    return freshAction;
};

SearchService.prototype.textSearch = function (text, action) {
    var searchResult = this.pageQuerier.findTextSearch(text);

    if (searchResult.found) {
        var digIdUsed = -1;
        if (action.digId !== undefined &&
            action.digId !== searchResult.found.dataset.digId &&
            action.prevQueries[0].textQuery.text === text) {
            digIdUsed = action.digId;
            searchResult.found.dataset.digId = digIdUsed.toString();
        } else {
            digIdUsed = this.issueDigId();
            searchResult.found.dataset.digId = digIdUsed.toString();
        }

        return {
            found: searchResult.found,
            digId: digIdUsed,
            closestMatches: []
        };
    } else {
        searchResult.allStrings.sort(function (a, b) {
            return a.distance - b.distance;
        });

        return {
            found: null,
            digId: null,
            closestMatches: searchResult.allStrings.map(function (a) {
                return a.term
            })
        };
    }
};

SearchService.prototype.spacialSearchFromAnchor = function (node, action) {
    var freshAction = this.removePrevQueries(action);

    var searchResult = this.pageQuerier.spacialSearch(node, freshAction);
    if (searchResult.found) {
        var digIdUsed = -1;
        if (action.digId.toString() !== node.dataset.digId) {
            digIdUsed = action.digId;
            searchResult.found.dataset.digId = digIdUsed.toString();
        } else {
            digIdUsed = this.issueDigId();
            searchResult.found.dataset.digId = digIdUsed.toString();
        }

        return {
            found: searchResult.found,
            digId: digIdUsed,
            closeResults: []
        };
    } else {
        return {
            found: null,
            digId: null,
            closeResults: searchResult.closeResults
        };
    }
};
