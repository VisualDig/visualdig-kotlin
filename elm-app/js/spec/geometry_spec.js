import Geometry from '../geometry.js';

describe("Geometry", function () {
    var geometry;

    describe("#rectIntersect", function () {
        it("happy path no right bound", function () {
            geometry = new Geometry();

            var rect = {
                right: null,
                bottom: 121,
                left: 2,
                top: 81
            };


            var actualResult = geometry.rectIntersect(rect, {
                x: 400000,
                y: 82
            });


            expect(actualResult).toBe(true);
        });

        it("happy path no left bound", function () {
            geometry = new Geometry();

            var rect = {
                right: 100,
                bottom: 121,
                left: null,
                top: 81
            };


            var actualResult = geometry.rectIntersect(rect, {
                x: -400000,
                y: 120
            });


            expect(actualResult).toBe(true);
        });

        it("happy path no top bound", function () {
            geometry = new Geometry();

            var rect = {
                right: 121,
                bottom: 100,
                left: 81,
                top: null
            };


            var actualResult = geometry.rectIntersect(rect, {
                x: 90,
                y: -400000
            });


            expect(actualResult).toBe(true);
        });

        it("happy path no bottom bound", function () {
            geometry = new Geometry();

            var rect = {
                right: 121,
                bottom: null,
                left: 81,
                top: 102
            };


            var actualResult = geometry.rectIntersect(rect, {
                x: 100,
                y: 400000
            });


            expect(actualResult).toBe(true);
        });

        it("outside of bound left", function () {
            geometry = new Geometry();

            var rect = {
                right: 121,
                bottom: null,
                left: 81,
                top: 102
            };


            var actualResult = geometry.rectIntersect(rect, {
                x: 80,
                y: 110
            });

            expect(actualResult).toBe(false);
        });

        it("outside of bound right", function () {
            geometry = new Geometry();

            var rect = {
                right: 121,
                bottom: null,
                left: 81,
                top: 102
            };


            var actualResult = geometry.rectIntersect(rect, {
                x: 122,
                y: 110
            });


            expect(actualResult).toBe(false);
        });

        it("outside of bound top", function () {
            geometry = new Geometry();

            var rect = {
                right: 121,
                bottom: null,
                left: 81,
                top: 102
            };


            var actualResult = geometry.rectIntersect(rect, {
                x: 100,
                y: 100
            });


            expect(actualResult).toBe(false);
        });

        it("outside of bound bottom", function () {
            geometry = new Geometry();

            var rect = {
                right: null,
                bottom: 121,
                left: 102,
                top: 81
            };


            var actualResult = geometry.rectIntersect(rect, {
                x: 110,
                y: 122
            });


            expect(actualResult).toBe(false);
        });
    });

    describe("#getRelativeCoords", function () {
        it("item right of anchor", function () {
            geometry = new Geometry();

            var anchor = {
                x: 10,
                y: 20
            };

            var otherCoords = {
                x: 20,
                y: 20
            };

            var actualResult = geometry.getRelativeCoords(anchor, otherCoords);


            expect(actualResult).toEqual({
                x: 10,
                y: 0
            });
        });

        it("item left of anchor", function () {
            geometry = new Geometry();

            var anchor = {
                x: 10,
                y: 20
            };

            var otherCoords = {
                x: 0,
                y: 20
            };

            var actualResult = geometry.getRelativeCoords(anchor, otherCoords);


            expect(actualResult).toEqual({
                x: -10,
                y: 0
            });
        });


        it("item above anchor", function () {
            geometry = new Geometry();

            var anchor = {
                x: 10,
                y: 20
            };

            var otherCoords = {
                x: 10,
                y: 10
            };

            var actualResult = geometry.getRelativeCoords(anchor, otherCoords);


            expect(actualResult).toEqual({
                x: 0,
                y: 10
            });
        });

        it("item below anchor", function () {
            geometry = new Geometry();

            var anchor = {
                x: 10,
                y: 20
            };

            var otherCoords = {
                x: 10,
                y: 30
            };

            var actualResult = geometry.getRelativeCoords(anchor, otherCoords);


            expect(actualResult).toEqual({
                x: 0,
                y: -10
            });
        });
    });

    describe("#euclideanDistance", function () {
        it("x component only", function () {
            geometry = new Geometry();

            var point1 = {
                x: 0,
                y: 0
            };

            var point2 = {
                x: 20,
                y: 0
            };


            var actualResult = geometry.euclideanDistance(point1, point2);


            expect(actualResult).toEqual(20);
        });

        it("y component only", function () {
            geometry = new Geometry();

            var point1 = {
                x: 0,
                y: 0
            };

            var point2 = {
                x: 0,
                y: 10
            };


            var actualResult = geometry.euclideanDistance(point1, point2);


            expect(actualResult).toEqual(10);
        });


        it("x and y components", function () {
            geometry = new Geometry();

            var point1 = {
                x: 0,
                y: 0
            };

            var point2 = {
                x: 5,
                y: 10
            };


            var actualResult = geometry.euclideanDistance(point1, point2);


            expect(actualResult).toBeCloseTo(11.180339);
        });
    });
});
