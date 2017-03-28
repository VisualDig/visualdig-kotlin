import PageQuerier from '../pageQuerier.js';
import Geometry from '../geometry.js';

describe("PageQuerier", function () {
    var pageQuerier, documentMock;

    describe("#spacialSearch", function () {
        it("happy path search east", function () {
            var treeWalkerStub = {
                nextNode: sinon.stub()
            };

            var createTreeWalkerStub = sinon.stub();
            createTreeWalkerStub.returns(treeWalkerStub);

            documentMock = {
                getElementById: function () {
                    return {
                        contentDocument: {
                            createTreeWalker: createTreeWalkerStub
                        }
                    }
                }
            };


            var node1 = {
                tagName: "DIV",
                type: undefined,
            };

            var foundNode2 = {
                tagName: "INPUT",
                type: "checkbox",
            };

            treeWalkerStub.nextNode
                .onFirstCall().returns(node1)
                .onSecondCall().returns(foundNode2)
                .onThirdCall().returns(null);

            pageQuerier = new PageQuerier(documentMock, new Geometry());
            var getAbsCoordinatesStub = sinon.stub(pageQuerier, "getAbsCoordinates")
            getAbsCoordinatesStub
                .onFirstCall().returns({
                    x: 15,
                    y: 115,
                    left: 0,
                    top: 100
                })
                .onSecondCall().returns({
                    x: 105,
                    y: 115,
                    left: 100,
                    top: 110
                });


            var actualResult = pageQuerier.spacialSearch(node1, {
                direction: "East",
                elementType: "Checkbox",
                tolerance: 10,
                priority: "AlignmentThenDistance",
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            sinon.assert.calledThrice(treeWalkerStub.nextNode);
            sinon.assert.calledTwice(getAbsCoordinatesStub);

            expect(actualResult).toEqual({
                result: "Success",
                found: foundNode2,
                closeResults: []
            });

        });

        it("fails to find item with no close matches", function () {
            var treeWalkerStub = {
                nextNode: sinon.stub()
            };

            var createTreeWalkerStub = sinon.stub();
            createTreeWalkerStub.returns(treeWalkerStub);

            documentMock = {
                getElementById: function () {
                    return {
                        contentDocument: {
                            createTreeWalker: createTreeWalkerStub
                        }
                    }
                }
            };

            var node1 = {
                tagName: "DIV",
                type: undefined,
            };

            var westNode = {
                tagName: "INPUT",
                type: "checkbox",
            };

            treeWalkerStub.nextNode
                .onFirstCall().returns(node1)
                .onSecondCall().returns(westNode)
                .onThirdCall().returns(null);

            pageQuerier = new PageQuerier(documentMock, new Geometry());
            var getAbsCoordinatesStub = sinon.stub(pageQuerier, "getAbsCoordinates")
            getAbsCoordinatesStub
                .onFirstCall().returns({
                    x: 315,
                    y: 110,
                    left: 300,
                    top: 100
                })
                .onSecondCall().returns({
                    x: 5,
                    y: 105,
                    left: 0,
                    top: 100
                });


            var actualResult = pageQuerier.spacialSearch(node1, {
                direction: "East",
                elementType: "Checkbox",
                tolerance: 10,
                priority: "AlignmentThenDistance",
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            sinon.assert.calledThrice(treeWalkerStub.nextNode);
            sinon.assert.calledTwice(getAbsCoordinatesStub);

            expect(actualResult).toEqual({
                result: "NoMatch",
                found: null,
                closeResults: []
            });

        });

        it("fails to find item with a close match", function () {
            var treeWalkerStub = {
                nextNode: sinon.stub()
            };

            var createTreeWalkerStub = sinon.stub();
            createTreeWalkerStub.returns(treeWalkerStub);

            documentMock = {
                getElementById: function () {
                    return {
                        contentDocument: {
                            createTreeWalker: createTreeWalkerStub
                        }
                    }
                }
            };

            var node1 = {
                tagName: "DIV",
                type: undefined,
            };

            var northWestNode = {
                id: 'secret-of-the-trees',
                class: 'false-wall',
                tagName: "INPUT",
                type: "checkbox",
            };

            treeWalkerStub.nextNode
                .onFirstCall().returns(node1)
                .onSecondCall().returns(northWestNode)
                .onThirdCall().returns(null);

            pageQuerier = new PageQuerier(documentMock, new Geometry());
            var getAbsCoordinatesStub = sinon.stub(pageQuerier, "getAbsCoordinates")
            getAbsCoordinatesStub
                .onFirstCall().returns({
                    x: 315,
                    y: 110,
                    left: 300,
                    top: 100
                })
                .onSecondCall().returns({
                    x: 405.3,
                    y: 85.9,
                    left: 400,
                    top: 80
                });


            var actualResult = pageQuerier.spacialSearch(node1, {
                direction: "East",
                elementType: "Checkbox",
                tolerance: 10,
                priority: "AlignmentThenDistance",
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            sinon.assert.calledThrice(treeWalkerStub.nextNode);
            sinon.assert.calledTwice(getAbsCoordinatesStub);

            expect(actualResult.closeResults.length).toEqual(1);
            expect(actualResult.closeResults[0].x).toEqual(90);
            expect(actualResult.closeResults[0].y).toEqual(24);

            expect(actualResult).toEqual({
                result: "NoMatch",
                found: null,
                closeResults: [{
                    x: 90,
                    y: 24,
                    tolerance: 10,
                    htmlClass: 'false-wall',
                    htmlId: 'secret-of-the-trees'
                }]
            });

        });

        it("resolves multiple results according to distance priority", function () {
            var treeWalkerStub = {
                nextNode: sinon.stub()
            };

            var createTreeWalkerStub = sinon.stub();
            createTreeWalkerStub.returns(treeWalkerStub);

            documentMock = {
                getElementById: function () {
                    return {
                        contentDocument: {
                            createTreeWalker: createTreeWalkerStub
                        }
                    }
                }
            };


            var node1 = {
                tagName: "DIV",
                type: undefined,
            };

            var foundNode2 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node2"

            };

            var foundNode3 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node3"
            };

            treeWalkerStub.nextNode
                .onFirstCall().returns(node1)
                .onSecondCall().returns(foundNode2)
                .onThirdCall().returns(foundNode3)
                .onCall(4).returns(null);

            pageQuerier = new PageQuerier(documentMock, new Geometry());
            var getAbsCoordinatesStub = sinon.stub(pageQuerier, "getAbsCoordinates")
            getAbsCoordinatesStub
                .onFirstCall().returns({
                    x: 15,
                    y: 115,
                    left: 0,
                    top: 100
                })
                .onSecondCall().returns({
                    x: 110,
                    y: 135,
                    left: 105,
                    top: 130
                })
                .onThirdCall().returns({
                    x: 100,
                    y: 95,
                    left: 95,
                    top: 90
                });


            var actualResult = pageQuerier.spacialSearch(node1, {
                direction: "East",
                elementType: "Checkbox",
                tolerance: 25,
                priority: "Distance",
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            expect(treeWalkerStub.nextNode.callCount).toEqual(4);
            expect(getAbsCoordinatesStub.callCount).toEqual(3);

            expect(actualResult).toEqual({
                result: "Success",
                found: foundNode3,
                closeResults: []
            });
        });

        it("multiple results with equal distance using distance priority", function () {
            var treeWalkerStub = {
                nextNode: sinon.stub()
            };

            var createTreeWalkerStub = sinon.stub();
            createTreeWalkerStub.returns(treeWalkerStub);

            documentMock = {
                getElementById: function () {
                    return {
                        contentDocument: {
                            createTreeWalker: createTreeWalkerStub
                        }
                    }
                }
            };


            var node1 = {
                tagName: "DIV",
                type: undefined,
            };

            var foundNode2 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node2"

            };

            var foundNode3 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node3"
            };

            treeWalkerStub.nextNode
                .onCall(0).returns(node1)
                .onCall(1).returns(foundNode2)
                .onCall(2).returns(foundNode3)
                .onCall(3).returns(null);

            pageQuerier = new PageQuerier(documentMock, new Geometry());
            var getAbsCoordinatesStub = sinon.stub(pageQuerier, "getAbsCoordinates")
            getAbsCoordinatesStub.onFirstCall().returns({
                x: 15,
                y: 115,
            });
            getAbsCoordinatesStub.onSecondCall().returns({
                x: 110,
                y: 135,
            });
            getAbsCoordinatesStub.onThirdCall().returns({
                x: 110,
                y: 95,
            });


            var actualResult = pageQuerier.spacialSearch(node1, {
                direction: "East",
                elementType: "Checkbox",
                tolerance: 25,
                priority: "Distance",
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            expect(treeWalkerStub.nextNode.callCount).toEqual(4);
            expect(getAbsCoordinatesStub.callCount).toEqual(3);

            expect(actualResult.found).toBeNull();
            expect([
                actualResult.closeResults[0].htmlId,
                actualResult.closeResults[1].htmlId
            ]).toContain('node2');

            expect([
                actualResult.closeResults[0].htmlId,
                actualResult.closeResults[1].htmlId
            ]).toContain('node3');

            expect(actualResult.result).toEqual('AmbiguousMatch');
        });

        it("resolves multiple results according to alignment priority", function () {
            var treeWalkerStub = {
                nextNode: sinon.stub()
            };

            var createTreeWalkerStub = sinon.stub();
            createTreeWalkerStub.returns(treeWalkerStub);

            documentMock = {
                getElementById: function () {
                    return {
                        contentDocument: {
                            createTreeWalker: createTreeWalkerStub
                        }
                    }
                }
            };


            var node1 = {
                tagName: "DIV",
                type: undefined,
            };

            var foundNode2 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node2"

            };

            var foundNode3 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node3"
            };

            treeWalkerStub.nextNode.onCall(0).returns(node1);
            treeWalkerStub.nextNode.onCall(1).returns(foundNode2);
            treeWalkerStub.nextNode.onCall(2).returns(foundNode3);
            treeWalkerStub.nextNode.onCall(3).returns(null);

            pageQuerier = new PageQuerier(documentMock, new Geometry());
            var getAbsCoordinatesStub = sinon.stub(pageQuerier, "getAbsCoordinates")
            getAbsCoordinatesStub.onFirstCall().returns({
                x: 15,
                y: 115,
                left: 0,
                top: 100
            });
            getAbsCoordinatesStub.onSecondCall().returns({
                x: 85,
                y: 125,
                left: 80,
                top: 130
            });
            getAbsCoordinatesStub.onThirdCall().returns({
                x: 105,
                y: 115,
                left: 100,
                top: 90
            });


            var actualResult = pageQuerier.spacialSearch(node1, {
                direction: "East",
                elementType: "Checkbox",
                tolerance: 25,
                priority: "Alignment",
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            expect(treeWalkerStub.nextNode.callCount).toEqual(4);
            expect(getAbsCoordinatesStub.callCount).toEqual(3);

            expect(actualResult).toEqual({
                result: "Success",
                found: foundNode3,
                closeResults: []
            });
        });

        it("multiple results with equal alignment using alignment priority", function () {
            var treeWalkerStub = {
                nextNode: sinon.stub()
            };

            var createTreeWalkerStub = sinon.stub();
            createTreeWalkerStub.returns(treeWalkerStub);

            documentMock = {
                getElementById: function () {
                    return {
                        contentDocument: {
                            createTreeWalker: createTreeWalkerStub
                        }
                    }
                }
            };


            var node1 = {
                tagName: "DIV",
                type: undefined,
            };

            var foundNode2 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node2"

            };

            var foundNode3 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node3"
            };

            treeWalkerStub.nextNode.onCall(0).returns(node1);
            treeWalkerStub.nextNode.onCall(1).returns(foundNode2);
            treeWalkerStub.nextNode.onCall(2).returns(foundNode3);
            treeWalkerStub.nextNode.onCall(3).returns(null);

            pageQuerier = new PageQuerier(documentMock, new Geometry());
            var getAbsCoordinatesStub = sinon.stub(pageQuerier, "getAbsCoordinates")
            getAbsCoordinatesStub.onFirstCall().returns({
                x: 15,
                y: 115,
            });
            getAbsCoordinatesStub.onSecondCall().returns({
                x: 85,
                y: 125,
            });
            getAbsCoordinatesStub.onThirdCall().returns({
                x: 105,
                y: 125,
            });


            var actualResult = pageQuerier.spacialSearch(node1, {
                direction: "East",
                elementType: "Checkbox",
                tolerance: 25,
                priority: "Alignment",
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            expect(treeWalkerStub.nextNode.callCount).toEqual(4);
            expect(getAbsCoordinatesStub.callCount).toEqual(3);

            expect(actualResult.found).toBeNull();
            expect([
                actualResult.closeResults[0].htmlId,
                actualResult.closeResults[1].htmlId
            ]).toContain('node2');

            expect([
                actualResult.closeResults[0].htmlId,
                actualResult.closeResults[1].htmlId
            ]).toContain('node3');

            expect(actualResult.result).toEqual('AmbiguousMatch');
        });

        it("resolves multiple results according to alignment then distance priority", function () {
            var treeWalkerStub = {
                nextNode: sinon.stub()
            };

            var createTreeWalkerStub = sinon.stub();
            createTreeWalkerStub.returns(treeWalkerStub);

            documentMock = {
                getElementById: function () {
                    return {
                        contentDocument: {
                            createTreeWalker: createTreeWalkerStub
                        }
                    }
                }
            };


            var node1 = {
                tagName: "DIV",
                type: undefined,
            };

            var checkboxNode2 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node2"

            };

            var checkboxNode3 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node3"
            };

            var checkboxNode4 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node4"
            };


            treeWalkerStub.nextNode.onCall(0).returns(node1);
            treeWalkerStub.nextNode.onCall(1).returns(checkboxNode2);
            treeWalkerStub.nextNode.onCall(2).returns(checkboxNode3);
            treeWalkerStub.nextNode.onCall(3).returns(checkboxNode4);
            treeWalkerStub.nextNode.onCall(4).returns(null);

            pageQuerier = new PageQuerier(documentMock, new Geometry());
            var getAbsCoordinatesStub = sinon.stub(pageQuerier, "getAbsCoordinates")
            getAbsCoordinatesStub.onCall(0).returns({
                x: 15,
                y: 125,
            });
            getAbsCoordinatesStub.onCall(1).returns({
                x: 135,
                y: 125,
            });
            getAbsCoordinatesStub.onCall(2).returns({
                x: 110,
                y: 125.1,
            });
            getAbsCoordinatesStub.onCall(3).returns({
                x: 80,
                y: 101,
            });


            var actualResult = pageQuerier.spacialSearch(node1, {
                direction: "East",
                elementType: "Checkbox",
                tolerance: 25,
                priority: "AlignmentThenDistance",
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            expect(treeWalkerStub.nextNode.callCount).toEqual(5);
            expect(getAbsCoordinatesStub.callCount).toEqual(4);

            expect(actualResult).toEqual({
                result: "Success",
                found: checkboxNode3,
                closeResults: []
            });
        });

        it("resolves multiple results according to distance then alignment priority", function () {
            var treeWalkerStub = {
                nextNode: sinon.stub()
            };

            var createTreeWalkerStub = sinon.stub();
            createTreeWalkerStub.returns(treeWalkerStub);

            documentMock = {
                getElementById: function () {
                    return {
                        contentDocument: {
                            createTreeWalker: createTreeWalkerStub
                        }
                    }
                }
            };


            var node1 = {
                tagName: "DIV",
                type: undefined,
            };

            var checkboxNode2 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node2"

            };

            var checkboxNode3 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node3"
            };

            var checkboxNode4 = {
                tagName: "INPUT",
                type: "checkbox",
                id: "node4"
            };


            treeWalkerStub.nextNode.onCall(0).returns(node1);
            treeWalkerStub.nextNode.onCall(1).returns(checkboxNode2);
            treeWalkerStub.nextNode.onCall(2).returns(checkboxNode3);
            treeWalkerStub.nextNode.onCall(3).returns(checkboxNode4);
            treeWalkerStub.nextNode.onCall(4).returns(null);

            pageQuerier = new PageQuerier(documentMock, new Geometry());
            var getAbsCoordinatesStub = sinon.stub(pageQuerier, "getAbsCoordinates")
            getAbsCoordinatesStub.onCall(0).returns({
                x: 15,
                y: 125,
            });
            getAbsCoordinatesStub.onCall(1).returns({
                x: 135,
                y: 125,
            });
            getAbsCoordinatesStub.onCall(2).returns({
                x: 110,
                y: 125,
            });
            getAbsCoordinatesStub.onCall(3).returns({
                x: 109.4,
                y: 135,
            });


            var actualResult = pageQuerier.spacialSearch(node1, {
                direction: "East",
                elementType: "Checkbox",
                tolerance: 25,
                priority: "DistanceThenAlignment",
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            expect(treeWalkerStub.nextNode.callCount).toEqual(5);
            expect(getAbsCoordinatesStub.callCount).toEqual(4);

            expect(actualResult).toEqual({
                result: "Success",
                found: checkboxNode3,
                closeResults: []
            });
        });
    });

    describe("#getAbsCoordinates", function () {
        it("happy path", function () {
            var bodyGetBoundingRectStub = sinon.stub();

            var createRangeStub = sinon.stub();
            var rangeBoundingClientRect = sinon.stub();

            var mockRange = {
                selectNodeContents: sinon.spy(),
                getBoundingClientRect: rangeBoundingClientRect
            };
            createRangeStub.returns(mockRange);
            rangeBoundingClientRect.returns({
                width: 2,
                height: 2,
                left: 0,
                top: 0,
            });

            documentMock = {
                body: {
                    getBoundingClientRect: bodyGetBoundingRectStub
                },
                createRange: createRangeStub
            };

            bodyGetBoundingRectStub.returns({
                width: 800,
                height: 600,
                left: 0,
                top: -100,
            });

            var node1 = {
                getBoundingClientRect: sinon.spy()
            };

            pageQuerier = new PageQuerier(documentMock);


            var actualResult = pageQuerier.getAbsCoordinates(node1, documentMock);


            sinon.assert.calledOnce(bodyGetBoundingRectStub);
            sinon.assert.calledOnce(createRangeStub);
            sinon.assert.calledOnce(rangeBoundingClientRect);
            sinon.assert.calledWith(mockRange.selectNodeContents, node1);
            expect(node1.getBoundingClientRect.notCalled).toBe(true);

            expect(actualResult).toEqual({
                left: 0,
                top: 100,
                x: 1,
                y: 101
            });

        });
    });

    describe("#getToleranceRect", function () {
        it("happy path east", function () {
            documentMock = {};

            pageQuerier = new PageQuerier(documentMock);

            var anchorCoords = {
                left: 0,
                top: 100,
                x: 1,
                y: 101
            };


            var actualResult = pageQuerier.getToleranceRect(20, anchorCoords, "East");


            expect(actualResult).toEqual({
                right: null,
                bottom: 121,
                left: 2,
                top: 81
            });
        });

        it("happy path west", function () {
            documentMock = {};

            pageQuerier = new PageQuerier(documentMock);

            var anchorCoords = {
                left: 100,
                top: 100,
                x: 101,
                y: 101
            };


            var actualResult = pageQuerier.getToleranceRect(20, anchorCoords, "West");


            expect(actualResult).toEqual({
                right: 100,
                bottom: 121,
                left: null,
                top: 81
            });
        });

        it("happy path north", function () {
            documentMock = {};

            pageQuerier = new PageQuerier(documentMock);

            var anchorCoords = {
                left: 100,
                top: 100,
                x: 101,
                y: 101
            };


            var actualResult = pageQuerier.getToleranceRect(20, anchorCoords, "North");


            expect(actualResult).toEqual({
                right: 121,
                bottom: 100,
                left: 81,
                top: null
            });
        });

        it("happy path south", function () {
            documentMock = {};

            pageQuerier = new PageQuerier(documentMock);

            var anchorCoords = {
                left: 100,
                top: 100,
                x: 101,
                y: 101
            };


            var actualResult = pageQuerier.getToleranceRect(20, anchorCoords, "South");


            expect(actualResult).toEqual({
                right: 121,
                bottom: null,
                left: 81,
                top: 102
            });
        });
    });
});
