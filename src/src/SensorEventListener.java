package src;

public interface SensorEventListener {
    void newSensor(String sensorClassName, int portNumber);
    void newSamples(float[][] sampleArray);
}
