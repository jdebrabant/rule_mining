/********************************************************************************
 * @author Justin Ardini
 * File:   SolarMain.java
 * @date   2/9/10
 * Asgn:   Solar
 ********************************************************************************/
package solar;

import jargs.gnu.CmdLineParser;

/**
 * Entry point for the simulation.  Apart from the main loop, SolarMain handles
 * reading XML from an input file and writing XML to an output file.
 */
public final class SolarMain {

    private SolarTimeInfo timeInfo;
    private SolarBodyManager solarBodyManager;
    private String outFile;

    /**
     * Constructor when an input file is specified.  Both inFile and outFile
     * are assumed to be non-null.
     * @param inFile - A non-null String representing an XML file.
     * @param outFile - A non-null String representing a file to be written.
     */
    public SolarMain(String inFile, String outFile) {
        timeInfo = new SolarTimeInfo();
        solarBodyManager = new SolarBodyListManager();
        this.outFile = outFile;
        try {
            SolarXML.parseInput(inFile, timeInfo, solarBodyManager);
        } catch (SolarXMLInputException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Constructor when no input file is specified.  All arguments are assumed
     * to be non-null.
     * @param outFile - A non-null String representing a valid file path.
     * @param numPlanets - The number of planets (Sun excluded) in the simulation
     * @param numTimesteps - The number of timesteps ran by the simulation
     */
    public SolarMain(String outFile, int numPlanets, int numTimesteps) {
        timeInfo = new SolarTimeInfo(0, SolarConstants.DEFAULT_TIME_STEP, numTimesteps);
        solarBodyManager = new SolarBodyListManager();
        this.outFile = outFile;
        createDefaultSimulation(numPlanets);
    }

    /**
     * Creates a randomized simulation including a central Sun and
     * numPlanets planets in orbit about the Sun.
     * @param numPlanets - The number of planets desired.  The simulation only
     * contains the Sun for numPlanets <= 0.
     */
    public final void createDefaultSimulation(int numPlanets) {
        SolarBody sun = solarBodyManager.createBody("Sun", SolarConstants.SUN_MASS, SolarConstants.SUN_RADIUS, "PLANET");
        sun.setPosition(0, 0, 0);
        sun.setVelocity(0, 0, 0);
        solarBodyManager.addBody(sun);
        // Adds all orbiting planets
        for (int i = 1; i <= numPlanets; i++) {
            String newName = "Planet" + i;
            SolarBody newBody = solarBodyManager.createBody(newName, 
SolarRandom.randomPlanetMass(), SolarConstants.PLANET_RADIUS, "PLANET");

            double randDist = SolarRandom.randomDistance();
            double randX = SolarRandom.randomX(randDist);
            double randY = SolarRandom.calculateY(randDist, randX);
            double randZ = SolarRandom.randomZ();
            newBody.setPosition(randX, randY, randZ);

            double velX = SolarPhysics.orbitVelocityX(newBody, sun);
            velX = SolarRandom.randomizeVelocity(velX);
            double velY = SolarPhysics.orbitVelocityY(newBody, velX);
            velY = SolarRandom.randomizeVelocity(velY);
            newBody.setVelocity(velX, velY, 0);
            solarBodyManager.addBody(newBody);
        }
    }

    /**
     * @return The current simulation time
     */
    public double getTime() {
        return timeInfo.getInitialTime() + (solarBodyManager.getTicks() * timeInfo.getTimestep());
    }

    /**
     * Runs the main loop for a Solar simulation.
     */
    public void runSimulation() {
        int stepsRun = 0;
        double timestep = timeInfo.getTimestep();
        double timestepsToRun = timeInfo.getTimestepsToRun();
        System.out.println("Timesteps passed :");
        while (stepsRun++ < timestepsToRun) {
            solarBodyManager.tick(timestep);
            if (stepsRun % 100 == 0)
            {
                System.out.println(String.format("%d out of %d", stepsRun, (int)timestepsToRun));
            }
        }
        saveCalled();
        System.exit(0);
    }

    /**
     * Outputs an XML file to the specified outputFile, if one was provided in
     * the SolarMain constructor.  Otherwise, outputs the Solar data to
     * default.xml.
     */
    public void saveCalled() {
        SolarXML.writeOutput(outFile, timeInfo, solarBodyManager, getTime());
    }

    /**
     * Start and initialize SolarMain
     * 
     * @param args - input and/or output file, number of planets,
     * number of timesteps
     */
    public static void main(String[] args) {

        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option inputFlag = parser.addStringOption('i', "input");
        CmdLineParser.Option outputFlag = parser.addStringOption('o', "output");
        CmdLineParser.Option numPlanetsFlag = parser.addIntegerOption('p', "planets");
        CmdLineParser.Option numTimestepsFlag = parser.addIntegerOption('t', "timesteps");

        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        String inputFile = (String) parser.getOptionValue(inputFlag);
        String outputFile = (String) parser.getOptionValue(outputFlag);
        int numPlanets = (Integer) parser.getOptionValue(numPlanetsFlag, 100);
        int numTimesteps = (Integer) parser.getOptionValue(numTimestepsFlag, 10000);

        // set a default output file
        if (outputFile == null) {
            outputFile = "default.xml";
        }
        SolarMain main;
        // Chooses the appropriate constructor based on the input arguments
        if (inputFile == null) {
            main = new SolarMain(outputFile, numPlanets, numTimesteps);
        } else {
            main = new SolarMain(inputFile, outputFile);
        }
        main.runSimulation();
    }
}
