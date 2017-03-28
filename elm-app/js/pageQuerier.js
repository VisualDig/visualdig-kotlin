export default function PageQuerier(document, geometry) {
    this.document = document;
    this.geometry = geometry;
    this.range = null;
}


PageQuerier.prototype.levenshteinDistance = function (a, b) {
    // courtesy of github.com/kigiri
    if (a.length === 0) return b.length
    if (b.length === 0) return a.length
    let tmp, i, j, prev, val, row
    // swap to save some memory O(min(a,b)) instead of O(a)
    if (a.length > b.length) {
        tmp = a
        a = b
        b = tmp
    }

    row = Array(a.length + 1)
    // init the row
    for (i = 0; i <= a.length; i++) {
        row[i] = i
    }

    // fill in the rest
    for (i = 1; i <= b.length; i++) {
        prev = i
        for (j = 1; j <= a.length; j++) {
            if (b[i - 1] === a[j - 1]) {
                val = row[j - 1] // match
            } else {
                val = Math.min(row[j - 1] + 1, // substitution
                    Math.min(prev + 1, // insertion
                        row[j] + 1)) // deletion
            }
            row[j - 1] = prev
            prev = val
        }
        row[a.length] = prev
    }
    return row[a.length]
};


PageQuerier.prototype.getTextFromNode = function (node) {
    if (node.outerText && node.outerText !== "") {
        return node.outerText;
    } else if (node.type === "button" && node.value !== "") {
        return node.value;
    }
    return null;
};


PageQuerier.prototype.getElementByDigId = function (id) {
    var iframe = this.document.getElementById('siteUnderTest').contentDocument;
    var nodeList = iframe.querySelectorAll('[data-dig-id="' + id + '"]');
    if (nodeList.length > 0) {
        return nodeList[0];
    } else {
        return null;
    }
};


PageQuerier.prototype.findTextSearch = function (text) {
    var iframe = this.document.getElementById('siteUnderTest').contentDocument;
    var n, textElement = null,
        a = [],
        walk = iframe.createTreeWalker(iframe.body,
            NodeFilter.SHOW_ELEMENT,
            null,
            false);
    while (n = walk.nextNode()) {
        var textFromNode = this.getTextFromNode(n);
        if (textFromNode) {
            if (textFromNode.search(text) != -1) {
                textElement = n;
            }
            var distance = this.levenshteinDistance(text.toLowerCase(), textFromNode.toLowerCase());
            a.push({
                term: textFromNode,
                distance: distance
            });
        }
    }

    return {
        result: textElement ? "Success" : "NoMatch",
        allStrings: a,
        found: textElement
    };
};


PageQuerier.prototype.clickPageNode = function (node) {
    node.click();
};


PageQuerier.prototype.spacialSearch = function (anchorNode, spacialSearchQuery) {
    var formatCloseResult = function (relativeCoords, node, spacialSearchQuery) {
        return {
            htmlId: node.id,
            htmlClass: node.class,
            tolerance: spacialSearchQuery.tolerance,
            x: Math.round(relativeCoords.x),
            y: Math.round(relativeCoords.y)
        };
    };

    var iframe = this.document.getElementById('siteUnderTest').contentDocument;
    var n, textElement = null,
        a = [],
        walk = iframe.createTreeWalker(iframe.body,
            NodeFilter.SHOW_ELEMENT,
            null,
            false);

    var anchorCoords = this.getAbsCoordinates(anchorNode, iframe);
    var toleranceRect = this.getToleranceRect(spacialSearchQuery.tolerance, anchorCoords, spacialSearchQuery.direction);
    var closeResults = []
    var succeedResults = []

    while (n = walk.nextNode()) {
        if (spacialSearchQuery.elementType === "Checkbox") {
            if (n.tagName === "INPUT" &&
                n.type === "checkbox") {
                var checkboxCoords = this.getAbsCoordinates(n, iframe);
                if (this.geometry.rectIntersect(toleranceRect, checkboxCoords)) {
                    var relativeCoords = this.geometry.getRelativeCoords(anchorCoords, checkboxCoords);
                    succeedResults.push({
                        found: n,
                        relativeCoords: relativeCoords,
                        distance: this.geometry.euclideanDistance(anchorCoords, checkboxCoords),
                        alignment: this.getAlignmentScore(spacialSearchQuery.direction, relativeCoords),
                        closeResults: []
                    });
                } else {
                    var closeMatchRect = this.getToleranceRect(spacialSearchQuery.tolerance * 3, anchorCoords, spacialSearchQuery.direction);
                    if (this.geometry.rectIntersect(closeMatchRect, checkboxCoords)) {
                        var relativeCoords = this.geometry.getRelativeCoords(anchorCoords, checkboxCoords);
                        var singleResult = formatCloseResult(relativeCoords, n, spacialSearchQuery);
                        closeResults.push(singleResult);
                    }
                }
            }
        } else {
            throw "Element type not supported yet!";
        }
    }

    if (succeedResults.length == 1) {
        delete succeedResults[0].distance;
        delete succeedResults[0].alignment;
        delete succeedResults[0].relativeCoords;

        succeedResults[0].result = "Success";
        return succeedResults[0];

    } else if (succeedResults.length > 1) {
        var isPrecise = this.sortByPriority(spacialSearchQuery.priority, succeedResults);

        if (!isPrecise) {
            var ambiguousResults = succeedResults.map(function (item) {
                return formatCloseResult(item.relativeCoords,
                    item.found,
                    spacialSearchQuery
                );
            });
            return {
                result: "AmbiguousMatch",
                found: null,
                closeResults: ambiguousResults
            }
        }

        delete succeedResults[0].alignment;
        delete succeedResults[0].distance;
        delete succeedResults[0].relativeCoords;

        succeedResults[0].result = "Success";
        return succeedResults[0];
    }

    return {
        result: "NoMatch",
        found: null,
        closeResults: closeResults
    }
};


PageQuerier.prototype.getAbsCoordinates = function (node, doc) {
    var bodyRect = doc.body.getBoundingClientRect();
    this.range = this.range || doc.createRange();
    this.range.selectNodeContents(node);
    var nodeRect = this.range.getBoundingClientRect();

    return {
        left: nodeRect.left - bodyRect.left,
        top: nodeRect.top - bodyRect.top,
        x: (nodeRect.left - bodyRect.left) + nodeRect.width / 2,
        y: (nodeRect.top - bodyRect.top) + nodeRect.height / 2
    };
};


PageQuerier.prototype.sortByPriority = function (priority, results) {
    if (priority === "Distance") {
        results.sort(function (a, b) {
            return a.distance - b.distance;
        });

        if (Math.round(results[0].distance) == Math.round(results[1].distance)) {
            return false;
        }
    } else if (priority === "Alignment") {
        results.sort(function (a, b) {
            return a.alignment - b.alignment;
        });

        if (Math.round(results[0].alignment) == Math.round(results[1].alignment)) {
            return false;
        }
    } else if (priority === "AlignmentThenDistance") {
        results.sort(function (a, b) {
            var alignment = a.alignment - b.alignment;
            if (Math.round(alignment) === 0) {
                return a.distance - b.distance;
            }
            return Math.round(alignment);
        });
    } else if (priority === "DistanceThenAlignment") {
        results.sort(function (a, b) {
            var distance = a.distance - b.distance;
            if (Math.round(distance) === 0) {
                return a.alignment - b.alignment;
            }
            return Math.round(distance);
        });
    }
    return true;
};


PageQuerier.prototype.getAlignmentScore = function (direction, relativeCoords) {
    if (direction == "East") {
        return Math.abs(relativeCoords.y);

    } else if (direction == "West") {
        return Math.abs(relativeCoords.y);

    } else if (direction == "North") {
        return Math.abs(relativeCoords.x);

    } else if (direction == "South") {
        return Math.abs(relativeCoords.x);

    }
};


PageQuerier.prototype.getToleranceRect = function (tolerance, anchorCoords, direction) {
    if (direction == "East") {
        return {
            right: null,
            bottom: anchorCoords.y + tolerance,
            left: anchorCoords.x + 1,
            top: anchorCoords.y - tolerance
        };
    } else if (direction == "West") {
        return {
            right: anchorCoords.x - 1,
            bottom: anchorCoords.y + tolerance,
            left: null,
            top: anchorCoords.y - tolerance
        };
    } else if (direction == "North") {
        return {
            right: anchorCoords.x + tolerance,
            bottom: anchorCoords.y - 1,
            left: anchorCoords.x - tolerance,
            top: null
        };
    } else if (direction == "South") {
        return {
            right: anchorCoords.x + tolerance,
            bottom: null,
            left: anchorCoords.x - tolerance,
            top: anchorCoords.y + 1
        };
    }
};
