package models.map

/**
 * Model class for layer
 */
case class Calque(taille: Int, persist: Float) {

  private val matrix = Array.ofDim[Int](taille, taille)

  private def printMatrix(): String = {
    matrix.toList
      .map(_.toList
            .map(prettify)
            .mkString(""))
      .mkString("\n")
  }

  private def prettify(number: Int): String = {
    "<span style=\"width=5px;height=5px;"+{if(number<100) "background-color:blue" else "background-color:rgb("+(255-number)+","+(255-number)+","+(255-number)+")"}+"\">"+"" + "0" +"</span>"
  }

  override def toString: String = {
    //"Calque: size -> " + taille + "; persist = " + persist + "\n" +
    printMatrix
  }
}

object Calque {
  //last boy scoot Thomas GIOVANNINI
  /**
   * Fill the matrix with 0
   * @author Simon Ronciere
   * @return the filled matrix
   */
  def filledWith0(size: Int, persistance: Float): Calque = {
    val layer = Calque(size, persistance)
    for (i <- 0 until layer.taille; j <- 0 until layer.taille)
      layer.matrix(i)(j) = 0
    layer
  }

  //last boy scoot Thomas GIOVANNINI
  /**
   * Fill the matrix of Int with random number
   * @author Simon Ronciere
   * @return the filled matrix
   */
  def filledWithRandom(size: Int, persistance: Float): Calque = {
    val random = scala.util.Random
    val layer = Calque(size, persistance)
    for (i <- 0 until size; j <- 0 until size)
      layer.matrix(i)(j) = random.nextInt(256)
    layer
  }

  def generer_calque(frequence: Int, octaves: Int, persistance: Float, liss: Int, calque: Calque): String = {
    val taille = calque.taille

    // Generate a random layer
    val randomCalque = Calque.filledWithRandom(taille, 1)

    /* Remplissage des calque
    *  Des valeurs sont fixés dans le calque suivant la fréquence. Les valeurs entre chacunes de ces valeurs fixes sont
    *  interpolées et lissées.
    */
    def smooth(size: Int, layer: Calque, strength: Int): Calque = {
      for (coordX <- 0 until size; coordY <- 0 until size) {
        layer.matrix(coordX)(coordY) =
          valeur_interpolee(coordX, coordY, math.pow(frequence, strength).toInt, randomCalque)
      }
      layer
    }

    val calquesTravail = generateWorkingLayer(octaves, persistance, taille)
      .zipWithIndex
      .map{ indexedCalque => smooth(taille, indexedCalque._1, indexedCalque._2)}

    //var res = calquesTravail.mkString("\n")

    val sum_persistances = calquesTravail.map(_.persist).sum

    //ajout des calques successifs
    for (i <- 0 until taille) {
      for (j <- 0 until taille) {
        for (n <- 0 until octaves) {
          // ! claque param
          calque.matrix(i)(j) += (calquesTravail(n).matrix(i)(j) * calquesTravail(n).persist).toInt
          //normalisation
          calque.matrix(i)(j) = (calque.matrix(i)(j).toFloat / sum_persistances).toInt
        }
      }

      //lissage
      val lissage = Calque.filledWith0(taille, 0)

      for (x <- 0 until taille; y <- 0 until taille) {
        var a = 0
        var n = 0
        for (k <- (x - liss) to (x + liss); l <- (y - liss) to (y + liss)) {
          if ((k >= 0) && (k < taille) && (l >= 0) && (l < taille)) {
            n = n + 1
            a = a + calque.matrix(k)(l)
          }
        }
        lissage.matrix(x)(y) = a / n
      }
    }
    val res=calque.toString
    res
  }

  /**
   * Generate an array of working layer
   * @author Thomas GIOVANNINI
   * @param octaves the size of the array
   * @param persistance the layer persistance
   * @return the array of working layer filled with 0s
   */
  def generateWorkingLayer(octaves: Int, persistance: Float, taille: Int): List[Calque] = {
    val calquesTravail = new Array[Calque](octaves)
    for (i <- 0 until octaves) {
      val filledWith0Layer = Calque.filledWith0(taille, math.pow(persistance, i+1).toFloat)
      calquesTravail(i) = filledWith0Layer
    }
    calquesTravail.toList
  }

  def valeur_interpolee(xCoordinate: Int, yCoordinate: Int, frequence: Int, randomLayer: Calque): Int = {
    /* déterminations des bornes */
    //Détermination d'un pas en fonction de la fréquence
    val pas: Float = randomLayer.taille.toFloat / frequence.toFloat

    val qX: Int = (xCoordinate.toFloat / pas).toInt
    val borneInfX = (qX * pas).toInt
    var borneSupX = ((qX + 1) * pas).toInt

    if (borneSupX >= randomLayer.taille) borneSupX = randomLayer.taille - 1

    val qY = (yCoordinate.toFloat / pas).toInt
    val borneInfY = (qY * pas).toInt
    var borneSupY = ((qY + 1) * pas).toInt

    if (borneSupY >= randomLayer.taille) borneSupY = randomLayer.taille - 1

    /* récupérations des valeurs aléatoires aux bornes */
    val b00 = randomLayer.matrix(borneInfX)(borneInfY)
    val b01 = randomLayer.matrix(borneInfX)(borneSupY)
    val b10 = randomLayer.matrix(borneSupX)(borneInfY)
    val b11 = randomLayer.matrix(borneSupX)(borneSupY)

    val v1 = interpolate(b00, b01, borneSupY - borneInfY, yCoordinate - borneInfY)
    val v2 = interpolate(b10, b11, borneSupY - borneInfY, yCoordinate - borneInfY)
    val fin = interpolate(v1, v2, borneSupX - borneInfX, xCoordinate - borneInfX)

    fin
  }

  /**
   * Fill a matrix of tile with empty tiles
   * @author Simon RONCIERE
   * @return the initialized matrix
   */
  private def interpolate(y1: Int, y2: Int, n: Int, delta: Int): Int = {
    // interpolation non linéaire
    if (n == 0)
      return y1
    if (n == 1)
      return y2

    val a = delta / n

    val fac1 = 3 * math.pow(1 - a, 2) - 2 * math.pow(1 - a, 3)
    val fac2 = 3 * math.pow(a, 2) - 2 * math.pow(a, 3)

    (y1 * fac1 + y2 * fac2).toInt

  }
}
