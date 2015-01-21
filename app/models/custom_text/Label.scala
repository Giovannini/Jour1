package models.custom_text

/**
 * @author Thomas GIOVANNINI
 * Model for strings used as labels.
 * @param content of the label
 */
case class Label (content: String) extends AnyVal with Ordered[Label]{
    require(content.matches("^[A-Z][a-z0-9]*$"))

    /**
     * Compare with an other label
     * @param other label with which this one is compared
     * @return -1 if this label is lower than the other one
     *         0  if the two labels are the identical
     *         1  if this label is greater than the other one
     */
    def compare(other: Label) = content compareTo other.content

    override def toString = content
}
