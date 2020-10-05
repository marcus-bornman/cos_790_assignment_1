package com.marcusbornman.cos_790_assignment_1;

import distrgenalg.DistrGenAlg;
import uk.ac.qub.cs.itc2007.ExamTimetablingProblem;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		// Program arguments
		String parameterFilePath = args[0];
		String problemFilePath = args[1];
		long seed = Long.parseLong(args[2]);

		// Set up the problem instance
		ExamTimetablingProblem examTimetablingProblem = ExamTimetablingProblem.fromFile(problemFilePath);
		EvohypProblem evohypProblem = new EvohypProblem(examTimetablingProblem);

		// Set up the genetic algorithm
		DistrGenAlg genAlg = new DistrGenAlg(seed, ConstructiveHeuristicEngine.SUPPORTED_HEURISTICS, 4);
		genAlg.setParameters(parameterFilePath);
		genAlg.setProblem(evohypProblem);
		genAlg.setInitialMaxLength(examTimetablingProblem.exams.size());

		// Evolve the genetic algorithm
		genAlg.evolve();
	}
}
