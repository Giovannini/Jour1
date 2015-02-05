package controllers

import models.map.Layer
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html

/**
 * Test controller for the map generation
 */

object mapGene extends Controller{

  def index = Action{
    val output = Html(main)
    Ok(output)
  }

  private def  main: String = {
    val frequency = 20
    val octave = 35
    val persistence= 0.5f
    val lissage = 3
    val outputSize = 150

    val layer = Layer.generateLayer(frequency,octave,persistence,lissage,outputSize)
    layer.toString
  }
}
