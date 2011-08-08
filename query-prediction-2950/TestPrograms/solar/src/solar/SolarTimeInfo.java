package solar;

/**
 * Wrapper class for time information needed in a simulation.
 * @author jardini
 */
public class SolarTimeInfo {

    private double initialTime;
    private double timestep;
    private double timestepsToRun;

    /**
     * Default constructor sets all fields to 0.
     */
    public SolarTimeInfo() {
        this.initialTime = 0;
        this.timestep = 0;
        this.timestepsToRun = 0;
    }

    /**
     * Constructor to set all fields.
     * @param initialTime - The time at the start of the simulation.
     * @param timestep - The time step between updates, in seconds.
     * @param timestepsToRun - The number of timesteps run in the simulation.
     */
    public SolarTimeInfo(double initialTime, double timestep, int timestepsToRun) {
        this.initialTime = initialTime;
        this.timestep = timestep;
        this.timestepsToRun = timestepsToRun;
    }

    public double getInitialTime() {
        return this.initialTime;
    }

    public double getTimestep() {
        return this.timestep;
    }

    public double getTimestepsToRun() {
        return this.timestepsToRun;
    }

    /**
     *
     * @param initialTime - A non-negative initial time in seconds
     */
    public void setInitialTime(double initialTime) {
        if (initialTime >= 0) {
            this.initialTime = initialTime;
        } else {
            throw new IllegalArgumentException("Cannot set time to negative value");
        }
    }

    /**
     *
     * @param timestep - A non-negative time in seconds
     */
    public void setTimestep(double timestep) {
        if (timestep >= 0) {
            this.timestep = timestep;
        } else {
            throw new IllegalArgumentException("Cannot set time to negative value");
        }
    }

    /**
     *
     * @param timestepsToRun - A non-negative int
     */
    public void setTimestepsToRun(double timestepsToRun) {
        if (timestepsToRun >= 0) {
            this.timestepsToRun = timestepsToRun;
        }
    }
}
