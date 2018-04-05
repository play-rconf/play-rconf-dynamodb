package io.playrconf.provider;

final class Tuple2<A, B> {

    /**
     * Left item.
     */
    private A left;

    /**
     * Right item.
     */
    private B right;

    /**
     * Build an empty instance.
     */
    public Tuple2() {
    }

    /**
     * Build a new instance.
     *
     * @param left  Left item
     * @param right Right item
     */
    public Tuple2(final A left, final B right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Tuple2<?, ?> tuple = (Tuple2<?, ?>) o;
        if (!left.equals(tuple.left)) {
            return false;
        }
        return right.equals(tuple.right);
    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

    /**
     * Retrieves the left item.
     *
     * @return The left item
     */
    public A getLeft() {
        return this.left;
    }

    /**
     * Set the left item.
     *
     * @param left The left item
     */
    public void setLeft(final A left) {
        this.left = left;
    }

    /**
     * Retrieves the right item.
     *
     * @return The right item
     */
    public B getRight() {
        return this.right;
    }

    /**
     * Set the right item.
     *
     * @param right The left item
     */
    public void setRight(final B right) {
        this.right = right;
    }
}
