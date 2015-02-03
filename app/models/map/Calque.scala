package models.map

/**
 * Created by eisti on 2/1/15.
 */
case class Calque (taille: Int,
persist : Float){

  private var matrice = Array.ofDim[Int](taille, taille)


/**
 * Fill a matrix of Int with random number
 * @author Simon Ronciere
 * @return the initialized matrix
 */
private def fillRandom(): Unit = {
  val r = scala.util.Random
  for (i <- 0 until taille; j <- 0 until taille)
  matrice(i)(j) =r.nextInt(256)
}

  /**
   * Fill a matrix of Int with 0
   * @author Simon Ronciere
   * @return the initialized matrix
   */
   def init_calque(): Unit = {
    for (i <- 0 until taille; j <- 0 until taille)
      matrice(i)(j) =0

  }

   def generer_calque( frequence : Int,
                              octaves : Int,
                              persistance : Float,
                              liss : Int,
                              calque : Calque): String = {
    val taille = calque.taille
    val pas = taille / frequence
    var persist_courante = persistance

     var res=""

    // Calque aléatoire
    val random_c = Calque(taille, 1)
    random_c.fillRandom()

     res = res +" random_c = \n"+random_c.matricestring()+"\n\n\n"
    //Calques de travail
    // Array ou liste ? A voir
    val calquesTravail = new Array[Calque](octaves)
    for (i <- 0 until octaves) {
      val calque_tmp = new Calque(taille, persist_courante)
      calque_tmp.init_calque()
      calquesTravail(i) = calque_tmp
      persist_courante = persist_courante* persistance
    }

    // Attention var et pas val
    var f_courante = frequence
    // remplissage de calque

    for (n <- 0 until octaves) {
      for (i <- 0 until taille){
        for (j <- 0 until taille) {
          val a = valeur_interpolee(i, j, f_courante, random_c)
          //Getter
          calquesTravail(n).matrice(i)(j) = a
        }
      f_courante = f_courante * frequence
      }
      res = res+" calque n°"+n+" = \n"+calquesTravail(n).matricestring()+"\n\n\n"
    }

    var sum_persistances = 0.0

    for (i <- 0 until octaves) {
      sum_persistances = sum_persistances + calquesTravail(i).persist
    }

    //ajout des calques successifs
    for (i <- 0 until taille) {
      for (j <- 0 until taille) {
        for (n <- 0 until octaves) {
          // ! claque param
          calque.matrice(i)(j) = calque.matrice(i)(j) + (calquesTravail(n).matrice(i)(j) * calquesTravail(n).persist).toInt
          //normalisation
          calque.matrice(i)(j) = (calque.matrice(i)(j) / sum_persistances).toInt
        }
      }


      //lissage
      val lissage = new Calque(taille, 0)
      lissage.init_calque()

      for (x <- 0 until taille) {
        for (y <- 0 until taille) {
          var a = 0
          var n = 0
          for (k <- x - liss to x + liss)
            for (l <- y - liss to y + liss)
              if ((k >= 0) && (k < taille) && (l >= 0) && (l < taille)) {
                n = n + 1
                a = a + calque.matrice(k)(l)
              }
          lissage.matrice(x)(y) = a / n
        }

      }
    }
    res
  }

  /**
   * Fill a matrix of tile with empty tiles
   * @author Thomas GIOVANNINI
   * @return the initialized matrix
   */
  private def interpolate(y1 : Int,
                          y2 : Int,
                          n : Int,
                          delta : Int): Int = {
      // interpolation non linéaire
      if (n == 0)
        return y1
      if (n==1)
        return y2

      val a = delta/n

      val fac1 = 3*math.pow(1-a, 2) - 2*math.pow(1-a,3)
      val fac2 = 3*math.pow(a, 2) - 2*math.pow(a, 3)

    (y1*fac1 + y2*fac2 ).toInt

  }

  private def  valeur_interpolee( i : Int,
                                  j : Int,
                                  frequence : Int,
                                  r : Calque): Int = {
    /* déterminations des bornes */


    var pas = r.taille.toFloat/frequence.toFloat

    var q : Float = i/pas
    val borne1x : Int = (q*pas).toInt
    var borne2x : Int = ((q + 1)* pas).toInt

    if(borne2x >= r.taille)
      borne2x = r.taille-1

    q = j/pas
    val borne1y = (q*pas).toInt
    var borne2y = ((q + 1)*pas).toInt

    if(borne2y >= r.taille)
      borne2y = r.taille-1

    /* récupérations des valeurs aléatoires aux bornes */
    val b00 = r.matrice(borne1x)(borne1y)
    val b01 = r.matrice(borne1x)(borne2y)
    val b10 = r.matrice(borne2x)(borne1y)
    val b11 = r.matrice(borne2x)(borne2y)

    val v1  = interpolate(b00, b01, borne2y-borne1y, j-borne1y)
    val v2  = interpolate(b10, b11, borne2y-borne1y, j-borne1y)
    val fin = interpolate(v1, v2, borne2x-borne1x , i-borne1x)

    fin
  }


  private def matricestring():String={
    var res=""
    for (x <- 0 until taille){
      for (y <- 0 until taille){
        res=res + matrice(x)(y).toString+" | "
      }
      res=res + "\n"
    }
    res
  }

  override def toString(): String ={
    " taille = "+this.taille+"   \npersist = "+this.persist+" \n\nmat = \n"+matricestring()
  }
}
