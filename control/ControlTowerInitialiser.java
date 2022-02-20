package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;
import towersim.util.NoSpaceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Utility class that contains static methods for loading a control tower and associated entities
 * from files.
 */
public class ControlTowerInitialiser {
    /**
     * Loads the number of ticks elapsed from the given reader instance.
     * The contents of the reader should match the format specified in the tickWriter row of in the
     * table shown in ViewModel.saveAs().
     *
     * For an example of valid tick reader contents, see the provided saves/tick_basic.txt and
     * saves/tick_default.txt files.
     *
     * The contents read from the reader are invalid if any of the following conditions are true:
     * The number of ticks elapsed is not an integer (i.e. cannot be parsed by
     *      Long.parseLong(String)). The number of ticks elapsed is less than zero.
     *
     * @param reader reader from which to load the number of ticks elapsed
     * @return number of ticks elapsed
     * @throws MalformedSaveException if the format of the text read from the reader is invalid
     *                                according to the rules above
     * @throws IOException if an IOException is encountered when reading from the reader
     */
    public static long loadTick(Reader reader) throws MalformedSaveException, IOException {
        long tick;
        BufferedReader tickReader = new BufferedReader(reader);
        String line = tickReader.readLine();
        try {
            Long.parseLong(line);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException("The number of ticks elapsed is not an integer");
        }
        tick = Long.parseLong(line);
        if (tick < 0) {
            throw new MalformedSaveException("The number of ticks elapsed is less than zero");
        }
        return tick;
    }

    /**
     * Loads the list of all aircraft managed by the control tower from the given reader instance.
     * The contents of the reader should match the format specified in the aircraftWriter row of in
     * the table shown in ViewModel.saveAs().
     *
     * For an example of valid aircraft reader contents, see the provided saves/aircraft_basic.txt
     * and saves/aircraft_default.txt files.
     *
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     *     The number of aircraft specified on the first line of the reader is not an integer
     *          (i.e. cannot be parsed by Integer.parseInt(String)).
     *     The number of aircraft specified on the first line is not equal to the number of aircraft
     *          actually read from the reader.
     *     Any of the conditions listed in the Javadoc for readAircraft(String) are true.
     *
     * This method should call readAircraft(String).
     *
     * @param reader reader from which to load the list of aircraft
     * @return list of aircraft read from the reader
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is invalid
     *                              according to the rules above
     */
    public static List<Aircraft> loadAircraft(Reader reader)
            throws IOException, MalformedSaveException {
        LinkedList<Aircraft> loadAircraft = new LinkedList<>();
        BufferedReader aircraftReader = new BufferedReader(reader);
        String line = aircraftReader.readLine();
        int numAirplanes;
        try {
            Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException("The number of aircraft specified on the first line"
                    + " of the reader is not an integer");
        }
        numAirplanes = Integer.parseInt(line);

        for (int i = 0; i < numAirplanes; i++) {
            line = aircraftReader.readLine();
            if (line == null) {
                throw new MalformedSaveException("The number of aircraft specified on the first "
                        + "line is not equal to the number of aircraft actually read from the "
                        + "reader");
            } else {
                loadAircraft.add(readAircraft(line));
            }
        }
        return loadAircraft;
    }

    /**
     * Loads the takeoff queue, landing queue and map of loading aircraft from the given reader
     *      instance.
     *
     * Rather than returning a list of queues, this method does not return anything. Instead, it
     *      should modify the given takeoff queue, landing queue and loading map by adding aircraft,
     *      etc.
     *
     * The contents of the reader should match the format specified in the queuesWriter row of in
     *      the table shown in ViewModel.saveAs().
     *
     * For an example of valid queues reader contents, see the provided saves/queues_basic.txt and
     *      saves/queues_default.txt files.
     *
     * The contents read from the reader are invalid if any of the conditions listed in the Javadoc
     *      for readQueue(BufferedReader, List, AircraftQueue) and
     *      readLoadingAircraft(BufferedReader, List, Map) are true.
     *
     * This method should call readQueue(BufferedReader, List, AircraftQueue) and
     *      readLoadingAircraft(BufferedReader, List, Map).z`
     *
     * @param reader reader from which to load the queues and loading map
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param takeoffQueue empty takeoff queue that aircraft will be added to
     * @param landingQueue empty landing queue that aircraft will be added to
     * @param loadingAircraft empty map that aircraft and loading times will be added to
     * @throws MalformedSaveException if the format of the text read from the reader is invalid
     *                                according to the rules above
     * @throws IOException if an IOException is encountered when reading from the reader
     */
    public static void loadQueues(Reader reader,
                                  List<Aircraft> aircraft,
                                  TakeoffQueue takeoffQueue,
                                  LandingQueue landingQueue,
                                  Map<Aircraft, Integer> loadingAircraft)
            throws MalformedSaveException, IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        readQueue(bufferedReader, aircraft, takeoffQueue);
        readQueue(bufferedReader, aircraft, landingQueue);
        readLoadingAircraft(bufferedReader, aircraft, loadingAircraft);

    }

    /**
     * Loads the list of terminals and their gates from the given reader instance.
     *
     * The contents of the reader should match the format specified in the terminalsWithGatesWriter
     *      row of in the table shown in ViewModel.saveAs().
     *
     * For an example of valid queues reader contents, see the provided
     *      saves/terminalsWithGates_basic.txt and saves/terminalsWithGates_default.txt files.
     *
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     *     The number of terminals specified at the top of the file is not an integer (i.e. cannot
     *          be parsed by Integer.parseInt(String)).
     *     The number of terminals specified is not equal to the number of terminals actually read
     *          from the reader.
     *     Any of the conditions listed in the Javadoc for
     *          readTerminal(String, BufferedReader, List) and readGate(String, List) are true.
     *
     * This method should call readTerminal(String, BufferedReader, List).
     *
     * @param reader reader from which to load the list of terminals and their gates
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return list of terminals (with their gates) read from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is invalid
     *                                according to the rules above
     * @throws IOException if an IOException is encountered when reading from the reader
     */
    public static List<Terminal> loadTerminalsWithGates(Reader reader, List<Aircraft> aircraft)
            throws MalformedSaveException, IOException {
        LinkedList<Terminal> terminals = new LinkedList<>();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        int numTerminals;

        try {
            Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException("The number of terminals specified at the top of "
                    + "the file is not an integer");
        }
        numTerminals = Integer.parseInt(line);

        for (int i = 0; i < numTerminals; i++) {
            terminals.add(readTerminal(bufferedReader.readLine(), bufferedReader, aircraft));
        }
        return terminals;
    }

    /**
     * Creates a control tower instance by reading various airport entities from the given readers.
     *
     * The following methods should be called in this order, and their results stored temporarily,
     * to load information from the readers:
     *
     *     loadTick(Reader) to load the number of elapsed ticks
     *     loadAircraft(Reader) to load the list of all aircraft
     *     loadTerminalsWithGates(Reader, List) to load the terminals and their gates
     *     loadQueues(Reader, List, TakeoffQueue, LandingQueue, Map) to load the takeoff queue,
     *          landing queue and map of loading aircraft to their loading time remaining
     *
     * Note: before calling loadQueues(), an empty takeoff queue and landing queue should be created
     * by calling their respective constructors. Additionally, an empty map should be created by
     * calling:
     *
     * new TreeMap<>(Comparator.comparing(Aircraft::getCallsign))
     *
     * This is important as it will ensure that the map is ordered by aircraft callsign
     * (lexicographically).
     *
     * Once all information has been read from the readers, a new control tower should be
     * initialised by calling ControlTower(long, List, LandingQueue, TakeoffQueue, Map). Finally,
     * the terminals that have been read should be added to the control tower by calling
     * ControlTower.addTerminal(Terminal).
     *
     * @param tick reader from which to load the number of ticks elapsed
     * @param aircraft reader from which to load the list of aircraft
     * @param queues reader from which to load the aircraft queues and map of loading aircraft
     * @param terminalsWithGates reader from which to load the terminals and their gates
     * @return  reader from which to load the terminals and their gates
     * @throws MalformedSaveException if reading from any of the given readers results in a
     *                                MalformedSaveException, indicating the contents of that reader
     *                                are invalid
     * @throws IOException if an IOException is encountered when reading from any of the readers
     */
    public static ControlTower createControlTower(Reader tick,
                                                  Reader aircraft,
                                                  Reader queues,
                                                  Reader terminalsWithGates)
            throws MalformedSaveException, IOException {
        ControlTower tower;
        long ticksElapsed = loadTick(tick);
        LinkedList<Aircraft> aircraftList = new LinkedList<>(loadAircraft(aircraft));
        LandingQueue landingQueue = new LandingQueue();
        TakeoffQueue takeoffQueue = new TakeoffQueue();
        Map<Aircraft, Integer> loadingAircraft =
                new TreeMap<>(Comparator.comparing(Aircraft::getCallsign));
        LinkedList<Terminal> terminals =
                new LinkedList<>(loadTerminalsWithGates(terminalsWithGates, aircraftList));
        loadQueues(queues, aircraftList, takeoffQueue, landingQueue, loadingAircraft);
        tower = new ControlTower(ticksElapsed,
                aircraftList, landingQueue,
                takeoffQueue, loadingAircraft);
        for (Terminal terminal : terminals) {
            tower.addTerminal(terminal);
        }
        return tower;
    }

    /**
     * Reads an aircraft from its encoded representation in the given string.
     *
     * If the AircraftCharacteristics.passengerCapacity of the encoded aircraft is greater than
     * zero, then a PassengerAircraft should be created and returned. Otherwise, a FreightAircraft
     * should be created and returned.
     *
     * The format of the string should match the encoded representation of an aircraft, as described
     * in Aircraft.encode().
     *
     * The encoded string is invalid if any of the following conditions are true:
     *
     *     More/fewer colons (:) are detected in the string than expected.
     *     The aircraft's AircraftCharacteristics is not valid, i.e. it is not one of those listed
     *          in AircraftCharacteristics.values().
     *     The aircraft's fuel amount is not a double (i.e. cannot be parsed by
     *          Double.parseDouble(String)).
     *     The aircraft's fuel amount is less than zero or greater than the aircraft's maximum fuel
     *          capacity.
     *     The amount of cargo (freight/passengers) onboard the aircraft is not an integer
     *          (i.e. cannot be parsed by Integer.parseInt(String)).
     *     The amount of cargo (freight/passengers) onboard the aircraft is less than zero or
     *          greater than the aircraft's maximum freight/passenger capacity.
     *     Any of the conditions listed in the Javadoc for readTaskList(String) are true.
     *
     * This method should call readTaskList(String).
     *
     * @param line line of text containing the encoded aircraft
     * @return decoded aircraft instance
     * @throws MalformedSaveException if the format of the given string is invalid according to the
     *                                rules above
     */
    public static Aircraft readAircraft(String line) throws MalformedSaveException {
        Aircraft aircraft;
        String[] aircraftDetails = line.split(":");

        if (aircraftDetails.length != 6) {
            throw new MalformedSaveException("More/fewer colons (:) are detected in the string"
                    + " than expected");
        }
        String callsign = aircraftDetails[0];
        AircraftCharacteristics aircraftCharacteristics;
        try {
            aircraftCharacteristics = AircraftCharacteristics.valueOf(aircraftDetails[1]);
        } catch (IllegalArgumentException e) {
            throw new MalformedSaveException("The aircraft's AircraftCharacteristics is not valid");
        }

        double fuelAmount;
        try {
            fuelAmount = Double.parseDouble(aircraftDetails[3]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException("The aircraft's fuel amount is not a double");
        }

        if ((fuelAmount < 0) || (fuelAmount > aircraftCharacteristics.fuelCapacity)) {
            throw new MalformedSaveException("The aircraft's fuel amount is less than zero or"
                    + " greater than the aircraft's maximum fuel capacity");
        }

        if (!aircraftDetails[4].equals("true") && !aircraftDetails[4].equals("false")) {
            throw new MalformedSaveException("Not true or false");
        }


        int cargo;
        try {
            cargo = Integer.parseInt(aircraftDetails[5]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException("The amount of cargo (freight/passengers) onboard "
                    + "the aircraft is not an integer");
        }

        if ((cargo < 0) || (aircraftCharacteristics.freightCapacity != 0
                && cargo > aircraftCharacteristics.freightCapacity)
                || (aircraftCharacteristics.passengerCapacity != 0
                && cargo > aircraftCharacteristics.passengerCapacity)) {
            throw new MalformedSaveException("The amount of cargo (freight/passengers) onboard "
                    + "the aircraft is less than zero or greater than the aircraft's maximum "
                    + "freight/passenger capacity");
        }

        TaskList taskList = readTaskList(aircraftDetails[2]);

        if (aircraftCharacteristics.passengerCapacity > 0) {
            aircraft = new PassengerAircraft(callsign, aircraftCharacteristics, taskList,
                    fuelAmount, cargo);
        } else {
            aircraft = new FreightAircraft(callsign, aircraftCharacteristics, taskList,
                    fuelAmount, cargo);
        }
        boolean emergency = aircraftDetails[4].equals("true");
        if (emergency) {
            aircraft.declareEmergency();
        }
        return aircraft;
    }

    /**
     * Reads a task list from its encoded representation in the given string.
     *
     * The format of the string should match the encoded representation of a task list, as described
     * in TaskList.encode().
     *
     * The encoded string is invalid if any of the following conditions are true:
     *
     *     The task list's TaskType is not valid (i.e. it is not one of those listed in
     *          TaskType.values()).
     *     A task's load percentage is not an integer (i.e. cannot be parsed by
     *          Integer.parseInt(String)).
     *     A task's load percentage is less than zero.
     *     More than one at-symbol (@) is detected for any task in the task list.
     *     The task list is invalid according to the rules specified in TaskList(List).
     *
     * @param taskListPart string containing the encoded task list
     * @return decoded task list instance
     * @throws MalformedSaveException if the format of the given string is invalid according to the
     *                                rules above
     */
    public static TaskList readTaskList(String taskListPart) throws MalformedSaveException {
        String[] tasks = taskListPart.split(",");
        LinkedList<Task> listOfTasks = new LinkedList<>();
        TaskList taskList;

        for (String task : tasks) {
            int loadPercent;
            String taskName = task.split("@")[0];
            if (task.split("@").length > 2) {
                throw new MalformedSaveException("More than one at-symbol (@) is detected "
                        + "for any task in the task list");
            }
            if (task.split("@").length == 1) {
                switch (taskName) {
                    case "AWAY":
                        listOfTasks.add(new Task(TaskType.AWAY));
                        break;
                    case "LAND":
                        listOfTasks.add(new Task(TaskType.LAND));
                        break;
                    case "WAIT":
                        listOfTasks.add(new Task(TaskType.WAIT));
                        break;
                    case "LOAD":
                        listOfTasks.add(new Task(TaskType.LOAD));
                        break;
                    case "TAKEOFF":
                        listOfTasks.add(new Task(TaskType.TAKEOFF));
                        break;
                    default:
                        throw new MalformedSaveException("The task list's TaskType is not valid");
                }
            } else if (task.split("@").length == 2) {
                try {
                    Integer.parseInt(task.split("@")[1]);
                    if (Integer.parseInt(task.split("@")[1]) < 0
                            || Integer.parseInt(task.split("@")[1]) > 100) {
                        throw new MalformedSaveException("A task's load percentage is less than "
                                + "zero or greater than 100");
                    }
                } catch (NumberFormatException e) {
                    throw new MalformedSaveException("terminalNumber or numGates not a number");
                }
                loadPercent = Integer.parseInt(task.split("@")[1]);
                switch (taskName) {
                    case "AWAY":
                        listOfTasks.add(new Task(TaskType.AWAY, loadPercent));
                        break;
                    case "LAND":
                        listOfTasks.add(new Task(TaskType.LAND, loadPercent));
                        break;
                    case "WAIT":
                        listOfTasks.add(new Task(TaskType.WAIT, loadPercent));
                        break;
                    case "LOAD":
                        listOfTasks.add(new Task(TaskType.LOAD, loadPercent));
                        break;
                    case "TAKEOFF":
                        listOfTasks.add(new Task(TaskType.TAKEOFF, loadPercent));
                        break;
                    default:
                        throw new MalformedSaveException("The task list's TaskType is not valid");
                }
            }


        }
        try {
            taskList = new TaskList(listOfTasks);
        } catch (IllegalArgumentException e) {
            throw new MalformedSaveException("The task list is invalid according to the rules"
                    + " specified in TaskList(List).");
        }

        return taskList;
    }

    /**
     * Reads an aircraft queue from the given reader instance.
     *
     * Rather than returning a queue, this method does not return anything. Instead, it should
     * modify the given aircraft queue by adding aircraft to it.
     *
     * The contents of the text read from the reader should match the encoded representation of an
     * aircraft queue, as described in AircraftQueue.encode().
     *
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     *     The first line read from the reader is null.
     *     The first line contains more/fewer colons (:) than expected.
     *     The queue type specified in the first line is not equal to the simple class name of the
     *          queue provided as a parameter.
     *     The number of aircraft specified on the first line is not an integer (i.e. cannot be
     *          parsed by Integer.parseInt(String)).
     *     The number of aircraft specified is greater than zero and the second line read is null.
     *     The number of callsigns listed on the second line is not equal to the number of aircraft
     *          specified on the first line.
     *     A callsign listed on the second line does not correspond to the callsign of any aircraft
     *          contained in the list of aircraft given as a parameter.
     *
     * @param reader reader from which to load the aircraft queue
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param queue empty queue that aircraft will be added to
     * @throws IOException if the format of the text read from the reader is invalid according to
     *                     the rules above
     * @throws MalformedSaveException if an IOException is encountered when reading from the reader
     */
    public static void readQueue(BufferedReader reader,
                                 List<Aircraft> aircraft,
                                 AircraftQueue queue) throws IOException, MalformedSaveException {
        String line = reader.readLine();
        System.out.println("Line: " + line);

        if (line == null) {
            throw new MalformedSaveException("The first line read from the reader is null");
        }

        if (line.split(":").length != 2) {
            throw new MalformedSaveException("The first line contains more/fewer colons (:)"
                    + "than expected");
        }
        if (!line.split(":")[0].equals(queue.getClass().getSimpleName())) {
            throw new MalformedSaveException("The queue type specified in the first line is not "
                    + "equal to the simple class name of the queue provided as a parameter");
        }
        try {
            Integer.parseInt(line.split(":")[1]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException("The number of aircraft specified on the first line"
                    + " is not an integer");
        }
        int numAircraft = Integer.parseInt(line.split(":")[1]);

        if (numAircraft > 0) {
            line = reader.readLine();
            System.out.println("Line2: " + line);
            if (line == null) {
                throw new MalformedSaveException("The number of aircraft specified is greater"
                        + " than zero and the second line read is null");
            } else if (line.split(",").length != numAircraft) {
                throw new MalformedSaveException("The number of callsigns listed on the second line"
                        + "is not equal to the number of aircraft specified on the first line");
            }
            for (int i = 0; i < numAircraft; i++) {
                Aircraft aircraft1 = null;
                for (Aircraft aircraft2 : aircraft) {
                    if (aircraft2.getCallsign().equals(line)) {
                        aircraft1 = aircraft2;
                    }
                }
                if (aircraft1 == null) {
                    throw new MalformedSaveException("A callsign listed on the second line does not"
                            + " correspond to the callsign of any aircraft contained in the list"
                            + " of aircraft given as a parameter");
                } else {
                    queue.addAircraft(aircraft1);
                }
            }
        }
    }

    /**
     * Reads the map of currently loading aircraft from the given reader instance.
     *
     * Rather than returning a map, this method does not return anything. Instead, it should modify
     * the given map by adding entries (aircraft/integer pairs) to it.
     *
     * The contents of the text read from the reader should match the format specified in the
     * queuesWriter row of in the table shown in ViewModel.saveAs(). Note that this method should
     * only read the map of loading aircraft, not the takeoff queue or landing queue. Reading these
     * queues is handled in the readQueue(BufferedReader, List, AircraftQueue) method.
     *
     * For an example of valid encoded map of loading aircraft, see the provided
     * saves/queues_basic.txt and saves/queues_default.txt files.
     *
     * The contents read from the reader are invalid if any of the following conditions are true:
     *
     *     The first line read from the reader is null.
     *     The number of colons (:) detected on the first line is more/fewer than expected.
     *     The number of aircraft specified on the first line is not an integer (i.e. cannot be
     *          parsed by Integer.parseInt(String)).
     *     The number of aircraft is greater than zero and the second line read from the reader is
     *          null.
     *     The number of aircraft specified on the first line is not equal to the number of
     *          callsigns read on the second line.
     *     For any callsign/loading time pair on the second line, the number of colons detected is
     *          not equal to one. For example, ABC123:5:9 is invalid.
     *     A callsign listed on the second line does not correspond to the callsign of any aircraft
     *          contained in the list of aircraft given as a parameter.
     *     Any ticksRemaining value on the second line is not an integer (i.e. cannot be parsed by
     *          Integer.parseInt(String)).
     *     Any ticksRemaining value on the second line is less than one (1).
     *
     * @param reader reader from which to load the map of loading aircraft
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param loadingAircraft empty map that aircraft and their loading times will be added to
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the text read from the reader is invalid
     *                                according to the rules above
     */
    public static void readLoadingAircraft(BufferedReader reader,
                                           List<Aircraft> aircraft,
                                           Map<Aircraft, Integer> loadingAircraft)
            throws IOException, MalformedSaveException {
        String line = reader.readLine();

        if (line == null) {
            throw new MalformedSaveException("The first line read from the reader is null");
        }

        if (line.split(":").length != 2) {
            throw new MalformedSaveException("The first line contains more/fewer colons (:)"
                    + "than expected");
        }
        if (!line.split(":")[0].equals("LoadingAircraft")) {
            throw new MalformedSaveException("The queue type specified in the first line is not "
                    + "equal to the simple class name of the queue provided as a parameter");
        }
        try {
            Integer.parseInt(line.split(":")[1]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException("The number of aircraft specified on the first line"
                    + " is not an integer");
        }
        int numAircraft = Integer.parseInt(line.split(":")[1]);
        if (numAircraft > 0) {
            line = reader.readLine();
            if (line == null) {
                throw new MalformedSaveException("The number of aircraft specified is greater"
                        + " than zero and the second line read is null");
            } else if (line.split(",").length != numAircraft) {
                throw new MalformedSaveException("The number of callsigns listed on the second line"
                        + " is not equal to the number of aircraft specified on the first line");
            }
            for (int i = 0; i < numAircraft; i++) {
                int loadingTime;
                if (line.split(",")[i].split(":").length != 2) {
                    throw new MalformedSaveException("For any callsign/loading time pair on the"
                            + " second line, the number of colons detected is  not equal to one");
                }
                try {
                    Integer.parseInt(line.split(",")[i].split(":")[1]);
                } catch (NumberFormatException e) {
                    throw new MalformedSaveException("Any ticksRemaining value on the second line"
                            + " is not an integer");
                }
                loadingTime = Integer.parseInt(line.split(",")[i].split(":")[1]);
                if (loadingTime < 0) {
                    throw new MalformedSaveException("Any ticksRemaining value on the second line"
                            + " is less than one");
                }
                Aircraft aircraft1 = readAircraft(line.split(",")[i].split(":")[0]);
                if (!aircraft.contains(aircraft1)) {
                    throw new MalformedSaveException("A callsign listed on the second line does"
                            + " not correspond to the callsign of any aircraft contained in the "
                            + "list of aircraft given as a parameter");
                }
                loadingAircraft.put(aircraft1, loadingTime);
            }
        }
    }

    /**
     * Reads a terminal from the given string and reads its gates from the given reader instance.
     *
     * The format of the given string and the text read from the reader should match the encoded
     * representation of a terminal, as described in Terminal.encode().
     *
     * For an example of valid encoded terminal with gates, see the provided
     * saves/terminalsWithGates_basic.txt and saves/terminalsWithGates_default.txt files.
     *
     * The encoded terminal is invalid if any of the following conditions are true:
     *
     *     The number of colons (:) detected on the first line is more/fewer than expected.
     *     The terminal type specified on the first line is neither AirplaneTerminal nor
     *          HelicopterTerminal.
     *     The terminal number is not an integer (i.e. cannot be parsed by
     *          Integer.parseInt(String)).
     *     The terminal number is less than one (1).
     *     The number of gates in the terminal is not an integer.
     *     The number of gates is less than zero or is greater than Terminal.MAX_NUM_GATES.
     *     A line containing an encoded gate was expected, but EOF (end of file) was received
     *          (i.e. BufferedReader.readLine() returns null).
     *     Any of the conditions listed in the Javadoc for readGate(String, List) are true.
     *
     * @param line string containing the first line of the encoded terminal
     * @param reader reader from which to load the gates of the terminal (subsequent lines)
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded terminal with its gates added
     * @throws IOException if an IOException is encountered when reading from the reader
     * @throws MalformedSaveException if the format of the given string or the text read from the
     *                                reader is invalid according to the rules above
     *
     */
    public static Terminal readTerminal(String line,
                                        BufferedReader reader,
                                        List<Aircraft> aircraft)
            throws IOException, MalformedSaveException {
        String[] terminalParts = line.split(":");
        String terminalType;
        String lineRead;
        int terminalNumber;
        int numGates;
        Terminal terminal;

        if (terminalParts.length != 4) {
            throw new MalformedSaveException("The number of colons (:) detected on the first "
                    + "line is more/fewer than expected");
        }
        terminalType = terminalParts[0];
        if ((!terminalType.equals("AirplaneTerminal"))
                && (!terminalType.equals("HelicopterTerminal"))) {
            throw new MalformedSaveException("The terminal type specified on the first line is "
                    + "neither AirplaneTerminal nor HelicopterTerminal");
        }
        try {
            terminalNumber = Integer.parseInt(terminalParts[1]);
            numGates = Integer.parseInt(terminalParts[3]);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException("terminalNumber or numGates not a number");
        }
        if (terminalType.equals("AirplaneTerminal")) {
            terminal = new AirplaneTerminal(terminalNumber);
        } else {
            terminal = new HelicopterTerminal(terminalNumber);
        }
        if ((!terminalParts[2].equals("true"))
                && (!terminalParts[2].equals("false"))) {
            throw new MalformedSaveException("Not an AirplaneTerminal or HelicopterTerminal");
        }
        if (terminalParts[2].equals("true")) {
            terminal.declareEmergency();
        }
        // LOOP numGates number of times and add gate
        for (int i = 0; i < numGates; i++) {
            lineRead = reader.readLine();
            if (lineRead != null) {
                try {
                    terminal.addGate(readGate(lineRead, aircraft));
                } catch (NoSpaceException e) {
                    e.printStackTrace();
                }

            } else {
                throw new IOException("IOException is encountered when reading from the reader");
            }
        }

        return terminal;
    }

    /**
     * Reads a gate from its encoded representation in the given string.
     *
     * The format of the string should match the encoded representation of a gate, as described in
     * Gate.encode().
     *
     * The encoded string is invalid if any of the following conditions are true:
     *
     *     The number of colons (:) detected was more/fewer than expected.
     *     The gate number is not an integer (i.e. cannot be parsed by Integer.parseInt(String)).
     *     The gate number is less than one (1).
     *     The callsign of the aircraft parked at the gate is not empty and the callsign does not
     *          correspond to the callsign of any aircraft contained in the list of aircraft given
     *          as a parameter.
     *
     * @param line string containing the encoded gate
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded gate instance
     * @throws MalformedSaveException if the format of the given string is invalid according to the
     *                                rules above
     */
    public static Gate readGate(String line, List<Aircraft> aircraft)
            throws MalformedSaveException {

        String[] gateParts = line.split(":");
        int gateNumber;
        Gate gate;
        Aircraft currentAircraft = null;
        if (gateParts.length != 2) {
            throw new MalformedSaveException("Invalid number of arguments");
        }
        try {

            gateNumber = Integer.parseInt(gateParts[0]);
            gate = new Gate(gateNumber);
        } catch (NumberFormatException e) {
            throw new MalformedSaveException("First argument isn't a number");
        }
        if (gateNumber < 1) {
            throw new MalformedSaveException("Gate number invalid");
        }
        String callsign = gateParts[1];
        if (!callsign.equals("empty")) {
            for (Aircraft aircraft1 : aircraft) {
                if (aircraft1.getCallsign().equals(callsign)) {
                    currentAircraft = aircraft1;
                }
            }
            if (currentAircraft == null) {
                throw new MalformedSaveException("No such aircraft found");
            }
            try {
                gate.parkAircraft(currentAircraft);
            } catch (NoSpaceException e) {
                e.printStackTrace();
            }
        }
        return gate;
    }
}
