package models.custom_types

/**
 * Model for strings used as labels.
 * @author Thomas GIOVANNINI
 * @param content of the label
 */
case class Label (content: String) {
    require(content.matches("^[A-Z][A-Za-z0-9 ]*$"))

    override def toString = content
}
