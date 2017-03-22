import PageQuerier from '../pageQuerier.js';
import SearchService from '../searchService.js';

describe("SearchService", function () {
    var searchService, pageQuerierMock;

    beforeEach(function () {
        pageQuerierMock = sinon.createStubInstance(PageQuerier);

        searchService = new SearchService(pageQuerierMock, 3);
    });

    describe("#spacialSearchFromAnchor", function () {
        it("happy path", function () {
            var mockResult = {
                found: {
                    dataset: {
                        digId: "-1"
                    }
                },
                closeResults: []
            };

            pageQuerierMock.spacialSearch.returns(mockResult);

            var spacialSearchDataBase = {
                direction: "East",
                elementType: "Checkbox",
                digId: 2
            };
            var spacialSearchData = Object.assign({}, spacialSearchDataBase);
            spacialSearchData.prevQueries = [{
                queryType: "TextQuery",
                textQuery: {
                    text: "foo bar"
                },
                spacialQuery: null
            }];

            var mockNode = {
                dataset: {
                    digId: "2"
                }
            };


            var actualSearchResult = searchService.spacialSearchFromAnchor(mockNode, spacialSearchData);


            expect(actualSearchResult).toEqual({
                found: mockResult.found,
                digId: 3,
                closeResults: []
            });

            expect(mockResult.found.dataset.digId).toEqual("3");
            sinon.assert.calledWith(pageQuerierMock.spacialSearch, mockNode, spacialSearchDataBase);
        });

        it("fails to find checkbox to the east", function () {
            var mockResult = {
                found: null,
                closeResults: [{
                    x: 100,
                    y: -30,
                    tolerance: 20,
                    htmlId: "really-good-id"
                }]
            };

            pageQuerierMock.spacialSearch.returns(mockResult);

            var spacialSearchDataBase = {
                direction: "East",
                elementType: "Checkbox",
                digId: 3
            };
            var spacialSearchData = Object.assign({}, spacialSearchDataBase);
            spacialSearchData.prevQueries = [{
                queryType: "TextQuery",
                textQuery: {
                    text: "foo bar"
                },
                spacialQuery: null
            }];

            var mockNode = {
                foo: "bar"
            };


            var actualSearchResult = searchService.spacialSearchFromAnchor(mockNode, spacialSearchData);


            expect(actualSearchResult).toEqual({
                found: null,
                digId: null,
                closeResults: [{
                    x: 100,
                    y: -30,
                    tolerance: 20,
                    htmlId: "really-good-id"
                }]
            });

            sinon.assert.calledWith(pageQuerierMock.spacialSearch, mockNode, spacialSearchDataBase);
        });
    });

    describe("#textSearch", function () {
        it("happy path", function () {
            var mockResult = {
                found: {
                    dataset: {
                        digId: "-1"
                    }
                }
            };

            pageQuerierMock.findTextSearch.returns(mockResult);

            var textSearchAction = "batman";


            var actualSearchResult = searchService.textSearch("batman", textSearchAction);


            expect(actualSearchResult).toEqual({
                found: mockResult.found,
                digId: 3,
                closestMatches: []
            });

            expect(mockResult.found.dataset.digId).toEqual("3");
            sinon.assert.calledWith(pageQuerierMock.findTextSearch, "batman");
        });

        it("click action search with no digId", function () {
            var mockResult = {
                found: {
                    dataset: {}
                }
            };

            pageQuerierMock.findTextSearch.returns(mockResult);

            var clickSearchAction = {
                digId: 1,
                prevQueries: [{
                    queryType: "TextQuery",
                    textQuery: {
                        text: "pow"
                    },
                    spacialQuery: null
                }]
            };

            var actualSearchResult = searchService.textSearch("pow", clickSearchAction);


            expect(actualSearchResult).toEqual({
                found: {
                    dataset: {
                        digId: "1"
                    }
                },
                digId: 1,
                closestMatches: []
            });

            sinon.assert.calledWith(pageQuerierMock.findTextSearch, "pow");
        });

        it("click action search with no digId but search term doesn't match previous query", function () {
            var mockResult = {
                found: {
                    dataset: {}
                }
            };

            pageQuerierMock.findTextSearch.returns(mockResult);

            var clickSearchAction = {
                digId: 1,
                prevQueries: [{
                    queryType: "TextQuery",
                    textQuery: {
                        text: "crew"
                    },
                    spacialQuery: null
                }]
            };

            var actualSearchResult = searchService.textSearch("pow", clickSearchAction);


            expect(actualSearchResult.digId).toEqual(3);

            sinon.assert.calledWith(pageQuerierMock.findTextSearch, "pow");
        });

        it("click action search that fails previous check", function () {
            var mockResult = {
                found: null,
                allStrings: [{
                    term: "powder",
                    distance: 3
                }, {
                    term: "Pow",
                    distance: 1
                }]
            };

            pageQuerierMock.findTextSearch.returns(mockResult);

            var clickSearchAction = {
                digId: 1,
                prevQueries: [{
                    queryType: "TextQuery",
                    textQuery: {
                        text: "pow"
                    },
                    spacialQuery: null
                }]
            };

            var actualSearchResult = searchService.textSearch("pow", clickSearchAction);


            expect(actualSearchResult).toEqual({
                found: null,
                digId: null,
                closestMatches: ["Pow", "powder"]
            });

            sinon.assert.calledWith(pageQuerierMock.findTextSearch, "pow");
        });
    });
});
