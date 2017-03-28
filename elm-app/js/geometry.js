export default function Geometry() {}

Geometry.prototype.rectIntersect = function (rect, point) {
    var inBoundsLeft = rect.left ? point.x >= rect.left : true;
    var inBoundsRight = rect.right ? point.x <= rect.right : true;
    var inBoundsTop = rect.top ? point.y >= rect.top : true;
    var inBoundsBottom = rect.bottom ? point.y <= rect.bottom : true;

    return inBoundsBottom && inBoundsLeft && inBoundsRight && inBoundsTop;
};

Geometry.prototype.getRelativeCoords = function (anchorCoords, nodeCoords) {
    return {
        x: nodeCoords.x - anchorCoords.x,
        y: anchorCoords.y - nodeCoords.y
    }
};

Geometry.prototype.euclideanDistance = function (point1, point2) {
    return Math.sqrt(Math.pow(point1.x - point2.x, 2) +
        Math.pow(point1.y - point2.y, 2));
};
