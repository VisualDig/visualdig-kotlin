import PortBindings from '../portBindings.js';
import PageQuerier from '../pageQuerier.js';

describe("PortBindings", function () {
    var portBindings, pageQuerierMock, elmMock;

    beforeEach(function () {
        pageQuerierMock = sinon.createStubInstance(PageQuerier);
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
                }
            }
        };

        portBindings = new PortBindings(elmMock, pageQuerierMock);
        expect(elmMock.ports.findText_search.subscribe.calledOnce).toBe(true);
        expect(elmMock.ports.click_searchText.subscribe.calledOnce).toBe(true);
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
                result: "Failure",
                message: "Text element not visible yet"
            });
        });
    });

    describe("#elmFindTextSearch", function () {
        it("happy path", function () {
            var call = elmMock.ports.findText_search.subscribe.getCall(0);
            var mockResult = {
                found: {
                    dataset: {
                        digId: -1
                    }
                }
            };

            pageQuerierMock.findTextSearch.returns(mockResult);

            // Calls function as if we were elm
            call.args[0]("batman");

            expect(mockResult.found.dataset.digId).not.toEqual(-1);
            sinon.assert.calledWith(pageQuerierMock.findTextSearch, "batman");
            sinon.assert.calledWith(elmMock.ports.findText_searchResult.send, {
                result: "Success",
                digId: 0,
                closestMatches: []
            });
        });

        it("fails to find text on page", function () {
            var call = elmMock.ports.findText_search.subscribe.getCall(0);
            var mockResult = {
                found: null,
                allStrings: [{
                    term: "pretzel",
                    distance: 10
                }, {
                    term: "Batman",
                    distance: 0
                }]
            };

            pageQuerierMock.findTextSearch.returns(mockResult);

            // Calls function as if we were elm
            call.args[0]("batman");

            sinon.assert.calledWith(pageQuerierMock.findTextSearch, "batman");
            sinon.assert.calledWith(elmMock.ports.findText_searchResult.send, {
                result: "Failure",
                digId: null,
                closestMatches: ["Batman", "pretzel"]
            });
        });
    });
});
