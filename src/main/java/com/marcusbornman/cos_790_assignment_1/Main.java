package com.marcusbornman.cos_790_assignment_1;

import distrgenalg.DistrGenAlg;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import uk.ac.qub.cs.itc2007.ExamTimetablingProblem;
import uk.ac.qub.cs.itc2007.ExamTimetablingSolution;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Main {
	public static void main(String[] args) throws ParseException, IOException {
		// Set up command line options
		Options options = new Options();
		options.addRequiredOption("p", "problemFile", true, "The problem instance file for the examination timetabling problem.");
		options.addRequiredOption("r", "randomSeed", true, "The seed used to generate random values.");
		options.addRequiredOption("o", "outputFile", true, "The folder to write the results of the experiment to.");
		options.addRequiredOption("g", "geneticParamFile", true, "The genetic parameters file required if 'search' is 'multipoint'");
		CommandLine cl = new DefaultParser().parse(options, args);

		// Set up problem instance
		System.out.println("Reading problem from " + cl.getOptionValue("p"));
		ExamTimetablingProblem problem = ExamTimetablingProblem.fromFile(cl.getOptionValue("p"));
		EvohypProblem evohypProblem = new EvohypProblem(problem);

		// Set up the genetic algorithm
		System.out.println("Setting up Genetic Algorithm with seed " + cl.getOptionValue("r") + " and parameters in " + cl.getOptionValue("g"));
		DistrGenAlg genAlg = new DistrGenAlg(Long.parseLong(cl.getOptionValue("r")), ConstructiveHeuristicEngine.SUPPORTED_HEURISTICS, 4);
		genAlg.setParameters(cl.getOptionValue("g"));
		genAlg.setProblem(evohypProblem);
		genAlg.setInitialMaxLength(problem.exams.size());

		System.out.println("Performing test...");
		runExperiment(genAlg, cl.getOptionValue("o"));
		System.out.println("Test complete. See " + cl.getOptionValue("o") + " for experiment results.");
	}

	private static void runExperiment(DistrGenAlg genAlg, String outputFile) throws IOException {
		LocalDateTime before = LocalDateTime.now();
		ExamTimetablingSolution solution = (ExamTimetablingSolution) genAlg.evolve().getSoln();
		LocalDateTime after = LocalDateTime.now();
		Duration runtime = Duration.between(before, after);

		List<String> lines = Arrays.asList(
				"=========================================================",
				"Test Result",
				"=========================================================",
				"Runtime (in milliseconds): " + runtime.toMillis(),
				"Distance to Feasibility: " + solution.distanceToFeasibility(),
				"Soft Constraint Violations: " + solution.softConstraintViolations(),
				"Objective Value: " + (solution.softConstraintViolations() * (solution.distanceToFeasibility() + 1)),
				"",
				"=========================================================",
				"Solution",
				"=========================================================",
				solution.toString()
		);

		//noinspection ResultOfMethodCallIgnored
		new File(outputFile).getParentFile().mkdirs();// to create file if it doesn't exist
		Files.write(Paths.get(outputFile), lines, StandardCharsets.UTF_8);
	}
}
