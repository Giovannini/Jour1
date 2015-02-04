package models.map

/**
 * Model class for layer
 */
case class Calque(taille: Int,
                  persist: Float) {

  private val matrix = Array.ofDim[Int](taille, taille)

  private def printMatrix(): String = {
    matrix.toList
      .map(_.toList
            .map(prettify)
            .mkString("|"))
      .mkString("\n")
  }

  private def prettify(number: Int): String = {
    "" + {if(number<100) "0" else ""} + {if(number<10) "0" else ""} + number.toString
  }

  override def toString: String = "Calque: size -> " + taille + "; persist = " + persist + "\n" + printMatrix
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
    //val pas = taille / frequence
    var res = ""

    // Generate a random layer
    val randomCalque = Calque.filledWithRandom(taille, 1)

    res = res + " random_c = \n" + randomCalque.printMatrix + "\n\n\n"
    //Calques de travail
    val calquesTravail = generateWorkingLayer(octaves, persistance, taille)

    // Attention var et pas val
    var f_courante = frequence

    // remplissage de calque
    for (n <- 0 until octaves) {
      for (i <- 0 until taille) {
        for (j <- 0 until taille) {
          val a = valeur_interpolee(i, j, f_courante, randomCalque)
          //Getter
          calquesTravail(n).matrix(i)(j) = a
        }
        f_courante = f_courante * frequence
      }
      res = res + " calque n°" + n + " = \n" + calquesTravail(n).printMatrix() + "\n\n\n"
    }

    /*var sum_persistances = 0.0

    for (i <- 0 until octaves) {
      sum_persistances = sum_persistances + calquesTravail(i).persist
    }*//**Replaced with*/
    val sum_persistances = calquesTravail.map(_.persist).sum

    //ajout des calques successifs
    for (i <- 0 until taille) {
      for (j <- 0 until taille) {
        for (n <- 0 until octaves) {
          // ! claque param
          calque.matrix(i)(j) += (calquesTravail(n).matrix(i)(j) * calquesTravail(n).persist).toInt
          //normalisation
          calque.matrix(i)(j) = (calque.matrix(i)(j) / sum_persistances).toInt
        }
      }

      //lissage
      val lissage = Calque.filledWith0(taille, 0)

      for (x <- 0 until taille) {
        for (y <- 0 until taille) {
          var a = 0
          var n = 0
          for (k <- x - liss to x + liss)
            for (l <- y - liss to y + liss)
              if ((k >= 0) && (k < taille) && (l >= 0) && (l < taille)) {
                n = n + 1
                a = a + calque.matrix(k)(l)
              }
          lissage.matrix(x)(y) = a / n
        }

      }
    }
    res
  }

  /**
   * Generate an array of working layer
   * @author Thomas GIOVANNINI
   * @param octaves the size of the array
   * @param persistance the layer persistance
   * @return the array of working layer filled with 0s
   */
  def generateWorkingLayer(octaves: Int, persistance: Float, taille: Int): Array[Calque] = {
    val calquesTravail = new Array[Calque](octaves)
    for (i <- 0 until octaves) {
      val filledWith0Layer = Calque.filledWith0(taille, math.pow(persistance, i+1).toFloat)
      calquesTravail(i) = filledWith0Layer
    }
    calquesTravail
  }

  def valeur_interpolee(i: Int, j: Int, frequence: Int, r: Calque): Int = {
    /* déterminations des bornes */
    val pas = r.taille.toFloat / frequence.toFloat

    var q: Float = i / pas
    val borne1x: Int = (q * pas).toInt
    var borne2x: Int = ((q + 1) * pas).toInt

    if (borne2x >= r.taille)
      borne2x = r.taille - 1

    q = j / pas
    val borne1y = (q * pas).toInt
    var borne2y = ((q + 1) * pas).toInt

    if (borne2y >= r.taille)
      borne2y = r.taille - 1

    /* récupérations des valeurs aléatoires aux bornes */
    val b00 = r.matrix(borne1x)(borne1y)
    val b01 = r.matrix(borne1x)(borne2y)
    val b10 = r.matrix(borne2x)(borne1y)
    val b11 = r.matrix(borne2x)(borne2y)

    val v1 = interpolate(b00, b01, borne2y - borne1y, j - borne1y)
    val v2 = interpolate(b10, b11, borne2y - borne1y, j - borne1y)
    val fin = interpolate(v1, v2, borne2x - borne1x, i - borne1x)

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
