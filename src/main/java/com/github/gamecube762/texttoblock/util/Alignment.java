package com.github.gamecube762.texttoblock.util;

/**
 * Text alignment.
 * Might not be noticeable on a single-lined string, but has a noticeable difference in multi-lined.
 *
 * <-       |       ->
 * Left, Center, Right. Shouldn't be too hard to understand.
 */
public enum Alignment {
    LEFT,
    CENTER,
    RIGHT;

    /**
     * Get the Alignment of the inputted String.
     *
     * @param s string
     * @return Alignment
     */
    public static Alignment of(String s) {
        for (Alignment a : values())
            if (a.name().toLowerCase().startsWith(s.toLowerCase()))//startswith allows us to accept C as Center
                return a;
        return null;
    }

    /**
     * Get the Alignment of the inputted Integer
     * 0 index
     *
     * 0 | left
     * 1 | center
     * 2 | right
     *
     * @param i in
     * @return Alignment
     */
    public static Alignment of(int i) {
        return values()[i];
    }
}
