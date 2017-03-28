package io.visualdig.spacial

enum class SearchPriority(val description: String) {
    /** alignment_then_distance is the default for all search actions.
     *               ___
     *              |   |
     *              | C |
     *     ___      |___|   ___      ___
     *    |   |            |   |    |   |
     *    | A |            | B |    | D |
     *    |___|            |___|    |___|
     *
     * In this arrangement, B would be chosen. B and D would
     * tie based on alignment, but if you factor in distance,
     * then B is closer. This is the definition of alignment_then_distance
     * search priority.
     */
    ALIGNMENT_THEN_DISTANCE("AlignmentThenDistance"),
    /**
     *               ___
     *              |   |
     *              | C |
     *     ___      |___|    ___
     *    |   |      |   |  |   |
     *    | A |      | B |  | D |
     *    |___|      |___|  |___|
     *
     * In this arrangement, B would be chosen. B and C would
     * tie based on distance alone, but if you factor in
     * alignment, then B is closer. This is the definition of
     * distance_then_alignment search priority.
     */
    DISTANCE_THEN_ALIGNMENT("DistanceThenAlignment"),

    /**
     *               ___
     *              |   |
     *              | C |
     *     ___      |___|   ___
     *    |   |            |   |
     *    | A |            | B |
     *    |___|            |___|
     *
     * In this arrangement, C would be chosen. C is closer
     * in terms of the euclidean distance between C and A.
     * If an equal distant element is found, an error will
     * occur.
     */
    DISTANCE("Distance"),

    /**
     *               ___
     *              |   |
     *              | C |
     *     ___      |___|   ___
     *    |   |            |   |
     *    | A |            | B |
     *    |___|            |___|
     *
     * In this arrangement, B would be chosen. C is closer
     * in terms of the euclidean distance between C and A.
     * If an equal alignemtn element is found, an error will
     * occur.
     */
    ALIGNMENT("Alignment"),
}