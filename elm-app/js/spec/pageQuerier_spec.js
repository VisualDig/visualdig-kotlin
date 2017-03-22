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
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            sinon.assert.calledTwice(treeWalkerStub.nextNode);
            sinon.assert.calledTwice(getAbsCoordinatesStub);

            expect(actualResult).toEqual({
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
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            sinon.assert.calledThrice(treeWalkerStub.nextNode);
            sinon.assert.calledTwice(getAbsCoordinatesStub);

            expect(actualResult).toEqual({
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
                digId: 2
            });


            sinon.assert.calledOnce(documentMock.getElementById().contentDocument.createTreeWalker);

            sinon.assert.calledThrice(treeWalkerStub.nextNode);
            sinon.assert.calledTwice(getAbsCoordinatesStub);

            expect(actualResult.closeResults.length).toEqual(1);
            expect(actualResult.closeResults[0].x).toEqual(90);
            expect(actualResult.closeResults[0].y).toEqual(24);

            expect(actualResult).toEqual({
                found: null,
                closeResults: [{
                    x: 90,
                    y: 24,
                    tolerance: 20,
                    htmlClass: 'false-wall',
                    htmlId: 'secret-of-the-trees'
                }]
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
