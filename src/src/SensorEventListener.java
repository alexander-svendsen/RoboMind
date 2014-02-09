package src;

public interface SensorEventListener {
    void initialize();
    void newSensor(Object sensorObject, String sensorClassName, int portNumber);
    void newInfo(String cmd, int portNumber);
    void newSamples(float[][] sampleArray);
}
