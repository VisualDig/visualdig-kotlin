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
        allStrings: a,
        found: textElement
    };
};

PageQuerier.prototype.clickPageNode = function (node) {
    node.click();
};

PageQuerier.prototype.spacialSearch = function (anchorNode, spacialSearchQuery, options = {}) {
    var iframe = this.document.getElementById('siteUnderTest').contentDocument;
    var n, textElement = null,
        a = [],
        walk = iframe.createTreeWalker(iframe.body,
            NodeFilter.SHOW_ELEMENT,
            null,
            false);

    var anchorCoords = this.getAbsCoordinates(anchorNode, iframe);
    var tolerance = options.tolerance || 20;
    var toleranceRect = this.getToleranceRect(tolerance, anchorCoords, spacialSearchQuery.direction);
    var closeResults = []

    while (n = walk.nextNode()) {
        if (spacialSearchQuery.elementType === "Checkbox") {
            if (n.tagName === "INPUT" &&
                n.type === "checkbox") {
                var checkboxCoords = this.getAbsCoordinates(n, iframe);
                if (this.geometry.rectIntersect(toleranceRect, checkboxCoords)) {
                    return {
                        found: n,
                        closeResults: []
                    };
                } else {
                    var closeMatchRect = this.getToleranceRect(tolerance * 3, anchorCoords, spacialSearchQuery.direction);
                    if (this.geometry.rectIntersect(closeMatchRect, checkboxCoords)) {
                        var singleResult = this.geometry.getRelativeCoords(anchorCoords, checkboxCoords);
                        singleResult.htmlId = n.id;
                        singleResult.tolerance = tolerance;
                        singleResult.htmlClass = n.class;
                        singleResult.x = Math.round(singleResult.x);
                        singleResult.y = Math.round(singleResult.y);
                        closeResults.push(singleResult);
                    }
                }
            }
        } else {
            throw "Element type not supported yet!";
        }
    }

    return {
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
