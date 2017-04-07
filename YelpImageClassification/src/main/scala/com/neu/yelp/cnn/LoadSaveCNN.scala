package com.neu.yelp.cnn


import java.io.{DataInputStream, DataOutputStream, File, FileInputStream}
import java.nio.file.{Files, Paths}
import org.apache.commons.io.FileUtils
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.factory.Nd4j


object LoadSaveCNN {

  def loadCNN(NNconfig: String, NNparams: String) = {
    // get neural network config
    val confFromJson: MultiLayerConfiguration = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(NNconfig)))

    // get neural network parameters
    val dis: DataInputStream = new DataInputStream(new FileInputStream(NNparams))
    val newParams = Nd4j.read(dis)

    // creating network object
    val savedNetwork: MultiLayerNetwork = new MultiLayerNetwork(confFromJson)
    savedNetwork.init()
    savedNetwork.setParameters(newParams)

    savedNetwork
  }

  def saveCNN(model: MultiLayerNetwork, NNconfig: String, NNparams: String) = {
    // save neural network config
    FileUtils.write(new File(NNconfig), model.getLayerWiseConfigurations().toJson())

    // save neural network parms
    val dos: DataOutputStream = new DataOutputStream(Files.newOutputStream(Paths.get(NNparams)))
    Nd4j.write(model.params(), dos)
  }


}