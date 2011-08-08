/********************************************************************************
 * @author Justin Ardini
 * File:   GalaxyMain.java
 * @date   2/22/10
 * Asgn:   Galaxy
 ********************************************************************************/
package galaxy;

import jargs.gnu.CmdLineParser;

/**
 * Entry point for the simulation.  Runs the main loop of the simulation, 
 * and implements SolarDraw.Control to interface with SolarDraw.
 */
public final class GalaxyMain {

    private TimeInfo timeInfo;
    private SolarBodyManager solarBodyManager;
    private String outFile;

    /**
     * Constructor when an input file is specified.  Both inFile and outFile
     * are assumed to be non-null.
     * @param inFile - A non-null String representing an XML file.
     * @param outFile - A non-null String representing a file to be written.
     */
    public GalaxyMain(String inFile, String outFile) {
        timeInfo = new TimeInfo();
        solarBodyManager = new SolarBodyOctreeManager();
        this.outFile = outFile;
        try {
            GalaxyXML.parseInput(inFile, timeInfo, solarBodyManager);
        } catch (XMLInputException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Constructor when no input file is specified.  All arguments are assumed
     * to be non-null.
     * @param outFile - A non-null String representing a valid file path.
     * @param numPlanets - The number of planets (Sun excluded) in the simulation
     * @param numRockets - The number of rockets in the simulation
     * @param numTimesteps - The number of timesteps ran by the simulation
     */
    public GalaxyMain(String outFile, int numPlanets, int numRockets, double numTimesteps) {
        timeInfo = new TimeInfo(0, SolarConstants.DEFAULT_TIME_STEP, numTimesteps);
        solarBodyManager = new SolarBodyOctreeManager();
        this.outFile = outFile;
        createDefaultSimulation(numPlanets, numRockets);
    }

    /**
     * Creates a randomized simulation including a central Sun and
     * numPlanets planets in orbit about the Sun.  The simulation
     * contains the Sun only if numPlanets <= 0 and numRockets <= 0.
     * @param numPlanets - The number of planets desired.
     * @param numRockets - The number of rockets desired.
     */
    public final void createDefaultSimulation(int numPlanets, int numRockets) {
        // Add the sun
        SolarBody sun = SolarBodyManager.createBody("Sun", SolarConstants.SUN_MASS, SolarConstants.SUN_RADIUS, "PLANET");
        solarBodyManager.addBody(sun);
        // Adds all orbiting planets
        for (int i = 1; i <= numPlanets; i++) {
            String newName = "Planet" + i;
            SolarBody newBody = SolarBodyManager.createBody(newName,
GalaxyRandom.randomPlanetMass(), SolarConstants.PLANET_RADIUS, "PLANET");

            double randDist = GalaxyRandom.randomDistance();
            double angle = GalaxyRandom.randomAngle();
            double randX = GalaxyRandom.randomX(randDist, angle);
            double randY = GalaxyRandom.randomY(randDist, angle);
            double randZ = GalaxyRandom.randomZ();
            newBody.setPosition(randX, randY, randZ);

            double velX = Physics.orbitVelocityX(newBody, sun);
            velX = GalaxyRandom.randomizeVelocity(velX);
            double velY = Physics.orbitVelocityY(newBody, velX);
            velY = GalaxyRandom.randomizeVelocity(velY);
            newBody.setVelocity(velX, velY, 0);

            solarBodyManager.addBody(newBody);
        }
        // Add all orbiting rockets
        for (int i = 1; i <= numRockets; i++) {
            String newName = "Rocket" + i;
            SolarBody newRocket = SolarBodyManager.createBody(newName, 
                    SolarConstants.ROCKET_MASS, SolarConstants.ROCKET_RADIUS, "ROCKET");

            double randDist = GalaxyRandom.randomDistance();
            double angle = GalaxyRandom.randomAngle();
            double randX = GalaxyRandom.randomX(randDist, angle);
            double randY = GalaxyRandom.randomY(randDist, angle);
            double randZ = GalaxyRandom.randomZ();
            newRocket.setPosition(randX, randY, randZ);

            double velX = Physics.orbitVelocityX(newRocket, sun);
            velX = GalaxyRandom.randomizeVelocity(velX);
            double velY = Physics.orbitVelocityY(newRocket, velX);
            velY = GalaxyRandom.randomizeVelocity(velY);
            newRocket.setVelocity(velX, velY, 0);

            solarBodyManager.addBody(newRocket);
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
        GalaxyXML.writeOutput(outFile, timeInfo, solarBodyManager, getTime());
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
        CmdLineParser.Option numRocketsFlag = parser.addIntegerOption('r', "rockets");
        CmdLineParser.Option numTimestepsFlag = parser.addDoubleOption('t', "timesteps");

        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
        }

        String inputFile = (String) parser.getOptionValue(inputFlag);
        String outputFile = (String) parser.getOptionValue(outputFlag);
        // Sets defaults for number of planets and rockets, and number of timesteps
        Integer numberOfPlanets = (Integer) parser.getOptionValue(numPlanetsFlag, 1000);
        //Integer numberOfRockets = (Integer) parser.getOptionValue(numRocketsFlag, 1000);
        Integer numberOfRockets = 0;
        Double timestepsToRun = (Double) parser.getOptionValue(numTimestepsFlag, 1000.0);

        // set a default output file
        if (outputFile == null) {
            outputFile = "default.xml";
        }
        GalaxyMain main;
        // Chooses the appropriate constructor based on the input arguments
        if (inputFile == null) {
            main = new GalaxyMain(outputFile, numberOfPlanets, numberOfRockets, timestepsToRun);
        } else {
            main = new GalaxyMain(inputFile, outputFile);
        }
        main.runSimulation();
    }
}
