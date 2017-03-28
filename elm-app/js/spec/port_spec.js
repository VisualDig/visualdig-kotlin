import PortBindings from '../portBindings.js';
import PageQuerier from '../pageQuerier.js';
import SearchService from '../searchService.js';

describe("PortBindings", function () {
    var portBindings, searchServiceMock, pageQuerierMock, elmMock;

    beforeEach(function () {
        pageQuerierMock = sinon.createStubInstance(PageQuerier);
        searchServiceMock = sinon.createStubInstance(SearchService);

        elmMock = {
            ports: {
                findText_search: {
                    subscribe: sinon.spy()
                },
                findText_searchResult: {
                    send: sinon.spy()
                },
                click_searchText: {
                    subscribe: sinon.spy()
                },
                click_searchResult: {
                    send: sinon.spy()
                },
                spacialSearch: {
                    subscribe: sinon.spy()
                },
                spacialSearch_result: {
                    send: sinon.spy()
                }
            }
        };

        portBindings = new PortBindings(elmMock, pageQuerierMock, searchServiceMock);
        expect(elmMock.ports.findText_search.subscribe.calledOnce).toBe(true);
        expect(elmMock.ports.click_searchText.subscribe.calledOnce).toBe(true);
        expect(elmMock.ports.spacialSearch.subscribe.calledOnce).toBe(true);
    });

    describe("#elmClickSearch", function () {
        it("happy path", function () {
            var call = elmMock.ports.click_searchText.subscribe.getCall(0);
            var mockNode = {
                click: function () {}
            };
            pageQuerierMock.getElementByDigId.returns(mockNode);
            pageQuerierMock.clickPageNode.returns(1);

            // Calls function as if we were elm
            call.args[0]({
                digId: 1,
                textQuery: "pow"
            });

            sinon.assert.calledOnce(pageQuerierMock.clickPageNode);
            sinon.assert.calledWith(pageQuerierMock.clickPageNode, mockNode);
            sinon.assert.calledWith(elmMock.ports.click_searchResult.send, {
                result: "Success",
                message: ""
            });
        });

        it("fail to find dig id (initiate another full text search)", function () {
            var call = elmMock.ports.click_searchText.subscribe.getCall(0);
            pageQuerierMock.getElementByDigId.returns(null);
            var mockResult = {
                found: {
                    dataset: {
                        digId: -1
                    },
                    click: function () {}
                }
            };

            pageQuerierMock.findTextSearch.returns(mockResult);

            pageQuerierMock.clickPageNode.returns(1);

            // Calls function as if we were elm
            call.args[0]({
                digId: 1,
                textQuery: "pow"
            });

            expect(mockResult.found.dataset.digId).toEqual(1);
            sinon.assert.calledWith(pageQuerierMock.findTextSearch, "pow");
            sinon.assert.calledWith(pageQuerierMock.clickPageNode, mockResult.found);
            sinon.assert.calledWith(elmMock.ports.click_searchResult.send, {
                result: "Success",
                message: ""
            });
        });

        it("fail to find dig id or element", function () {
            var call = elmMock.ports.click_searchText.subscribe.getCall(0);
            pageQuerierMock.getElementByDigId.returns(null);
            var mockResult = {
                found: null
            };

            pageQuerierMock.findTextSearch.returns(mockResult);

            pageQuerierMock.clickPageNode.returns(1);

            // Calls function as if we were elm
            call.args[0]({
                digId: 1,
                textQuery: "pow"
            });

            sinon.assert.calledWith(pageQuerierMock.findTextSearch, "pow");
            sinon.assert.calledWith(elmMock.ports.click_searchResult.send, {
                result: "Failure_QueryExpired",
                message: "Text element not visible yet"
            });
        });
    });

    describe("#elmFindTextSearch", function () {
        it("happy path", function () {
            var mockResult = {
                found: {
                    foo: "bar",
                    id: "foo-id"
                },
                digId: 11,
                closestMatches: []
            };

            searchServiceMock.textSearch.returns(mockResult);


            // Calls function as if we were elm
            var call = elmMock.ports.findText_search.subscribe.getCall(0);
            call.args[0]("batman");


            sinon.assert.calledWith(searchServiceMock.textSearch, "batman", "batman");
            sinon.assert.calledWith(elmMock.ports.findText_searchResult.send, {
                result: "Success",
                digId: 11,
                htmlId: "foo-id",
                closestMatches: []
            });
        });

        it("fails to find text on page", function () {
            var mockResult = {
                result: "NoMatch",
                found: null,
                digId: null,
                closestMatches: ["Batman", "pretzel"]
            };

            searchServiceMock.textSearch.returns(mockResult);

            // Calls function as if we were elm
            var call = elmMock.ports.findText_search.subscribe.getCall(0);
            call.args[0]("batman");

            sinon.assert.calledWith(searchServiceMock.textSearch, "batman", "batman");
            sinon.assert.calledWith(elmMock.ports.findText_searchResult.send, {
                result: "Failure_NoMatch",
                digId: null,
                htmlId: null,
                closestMatches: mockResult.closestMatches
            });
        });
    });

    describe("#elmSpacialSearch", function () {
        it("happy path", function () {
            var mockNode = {
                foo: "bar"

            };

            pageQuerierMock.getElementByDigId.returns(mockNode);

            var mockResult = {
                result: "Success",
                found: {
                    fooest: "bars",
                    id: "foo-id"
                },
                digId: 4,
                htmlId: "foo-id",
                closeResults: []
            };

            searchServiceMock.spacialSearchFromAnchor.returns(mockResult);

            var spacialSearchData = {
                direction: "East",
                elementType: "Checkbox",
                digId: 3,
                prevQueries: [{
                    queryType: "TextQuery",
                    textQuery: {
                        text: "foo bar"
                    },
                    spacialQuery: null
                }]
            };


            // Calls function as if we were elm
            var call = elmMock.ports.spacialSearch.subscribe.getCall(0);
            call.args[0](spacialSearchData);


            sinon.assert.calledWith(pageQuerierMock.getElementByDigId, 3);
            sinon.assert.calledWith(searchServiceMock.spacialSearchFromAnchor, mockNode, spacialSearchData);
            sinon.assert.calledWith(elmMock.ports.spacialSearch_result.send, {
                result: "Success",
                digId: 4,
                htmlId: "foo-id",
                closeResults: []
            });
        });

        it("fails to find checkbox to the east", function () {
            var mockNode = {
                foo: "bar"
            };

            pageQuerierMock.getElementByDigId.returns(mockNode);

            var mockResult = {
                result: "NoMatch",
                found: null,
                digId: null,
                closeResults: [{
                    x: 100,
                    y: -30,
                    tolerance: 20,
                    htmlId: "really-good-id"
                }]
            };

            searchServiceMock.spacialSearchFromAnchor.returns(mockResult);

            var spacialSearchData = {
                direction: "East",
                elementType: "Checkbox",
                digId: 3,
                prevQueries: [{
                    queryType: "TextQuery",
                    textQuery: {
                        text: "foo bar"
                    },
                    spacialQuery: null
                }]
            };


            // Calls function as if we were elm
            var call = elmMock.ports.spacialSearch.subscribe.getCall(0);
            call.args[0](spacialSearchData);


            sinon.assert.calledWith(pageQuerierMock.getElementByDigId, 3);
            sinon.assert.calledWith(searchServiceMock.spacialSearchFromAnchor, mockNode, spacialSearchData);

            var expectedResult = {
                result: "Failure_NoMatch",
                digId: null,
                htmlId: null,
                closeResults: [{
                    x: 100,
                    y: -30,
                    tolerance: 20,
                    htmlId: "really-good-id"
                }]
            };
            sinon.assert.calledWith(elmMock.ports.spacialSearch_result.send, expectedResult);
        });

        it("finds ambiguous match", function () {
            var mockNode = {
                foo: "bar"
            };

            pageQuerierMock.getElementByDigId.returns(mockNode);

            var mockResult = {
                result: "AmbiguousMatch",
                found: null,
                digId: null,
                closeResults: [{
                    x: 100,
                    y: -30,
                    tolerance: 20,
                    htmlId: "really-good-id"
                }]
            };

            searchServiceMock.spacialSearchFromAnchor.returns(mockResult);

            var spacialSearchData = {
                direction: "East",
                elementType: "Checkbox",
                digId: 3,
                prevQueries: [{
                    queryType: "TextQuery",
                    textQuery: {
                        text: "foo bar"
                    },
                    spacialQuery: null
                }]
            };


            // Calls function as if we were elm
            var call = elmMock.ports.spacialSearch.subscribe.getCall(0);
            call.args[0](spacialSearchData);


            sinon.assert.calledWith(pageQuerierMock.getElementByDigId, 3);
            sinon.assert.calledWith(searchServiceMock.spacialSearchFromAnchor, mockNode, spacialSearchData);

            var expectedResult = {
                result: "Failure_AmbiguousMatch",
                digId: null,
                htmlId: null,
                closeResults: [{
                    x: 100,
                    y: -30,
                    tolerance: 20,
                    htmlId: "really-good-id"
                }]
            };
            sinon.assert.calledWith(elmMock.ports.spacialSearch_result.send, expectedResult);
        });


        it("fails to find original element by id (redo text search)", function () {
            pageQuerierMock.getElementByDigId.returns(null);

            var mockTextSearchResult = {
                found: {
                    spin: "foos"
                }
            };

            pageQuerierMock.findTextSearch.returns(mockTextSearchResult);

            var mockResult = {
                found: {
                    foos: "ball",
                    id: "foo-id"
                },
                digId: 6,
                closeResults: []
            };

            searchServiceMock.spacialSearchFromAnchor.returns(mockResult);

            var spacialSearchData = {
                direction: "East",
                elementType: "Checkbox",
                digId: 3,
                prevQueries: [{
                    queryType: "TextQuery",
                    textQuery: {
                        text: "foo bar"
                    },
                    spacialQuery: null
                }]
            };


            // Calls function as if we were elm
            var call = elmMock.ports.spacialSearch.subscribe.getCall(0);
            call.args[0](spacialSearchData);


            sinon.assert.calledWith(pageQuerierMock.findTextSearch, "foo bar");
            sinon.assert.calledWith(pageQuerierMock.getElementByDigId, 3);
            sinon.assert.calledWith(searchServiceMock.spacialSearchFromAnchor, mockTextSearchResult.found, spacialSearchData);
            sinon.assert.calledWith(elmMock.ports.spacialSearch_result.send, {
                result: "Success",
                digId: 6,
                htmlId: "foo-id",
                closeResults: []
            });
        });
    });
});
