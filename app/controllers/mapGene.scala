package controllers

import models.map.Calque
import play.api.mvc.{Action, Controller}

/**
 * Test controller for the map generation
 */

object mapGene extends Controller{

  def index = Action{
    val output = main()
    Ok(output)
  }

  private def  main( ): String = {
    /* déterminations des bornes */
    val octave = 3
    val frequence = 4
    val persistance=1/2 : Float
    val taille_sortie = 50
    val lissage = 3

    val calque = Calque.filledWith0(taille_sortie,1)
    val matrice = Array.ofDim[Int](taille_sortie, taille_sortie)

    Calque.generer_calque(frequence,octave,persistance,lissage,calque)

    //calque.toString()

/*    val r = scala.util.Random
    for (i <- 0 until taille_sortie; j <- 0 until taille_sortie)
      matrice(i)(j) =r.nextInt(256)


    var res=""
    for (x <- 0 until taille_sortie){
      for (y <- 0 until taille_sortie){
        res=res + matrice(x)(y).toString +" | "
      }
      res=res + "\n"
    }
    res*/
  }
}
