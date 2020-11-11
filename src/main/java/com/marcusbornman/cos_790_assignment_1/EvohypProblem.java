package com.marcusbornman.cos_790_assignment_1;

import problemdomain.ProblemDomain;
import dev.born.itc2007.ExamTimetablingProblem;
import dev.born.itc2007.ExamTimetablingSolution;

import java.util.List;

public class EvohypProblem extends ProblemDomain {
	public final ExamTimetablingProblem problem;

	/**
	 * The constructor for the ExamTimetablingProblem class.
	 *
	 * @param problem - the examination timetabling problem instance that needs to be solved.
	 */
	public EvohypProblem(ExamTimetablingProblem problem) {
		this.problem = problem;
	}

	/**
	 * This method takes the heuristic combination heuristicComb evolved by
	 * the genetic algorithm in the package GenAlg or DistrGenAlg as input. The
	 * string specified in heuristicComb is used to construct a solution to the
	 * problem and returns an object of type ExamTimetablingSolution. An instance of the type
	 * ExamTimetablingSolution combines the objective value or function of the objective value
	 * that will be used as a fitness value for each chromosome by the genetic
	 * algorithm and the solution created using the evolved string of heuristics.
	 *
	 * @param heuristicComb - the heuristic combination evolved by the genetic
	 *                      algorithm in the package GenAlg or DistrGenAlg.
	 * @return an object of type ExamTimetablingSolution.
	 */
	@Override
	public EvohypSolution evaluate(String heuristicComb) {
		ExamTimetablingSolution solution = new ExamTimetablingSolution(problem, List.of());
		while (solution.bookings.size() != problem.exams.size()) {
			ConstructiveHeuristicEngine constructiveHeuristicEngine = new ConstructiveHeuristicEngine(problem);
			for (char character : heuristicComb.toCharArray()) {
				solution = constructiveHeuristicEngine.applyToSolution(character, solution);

				if (solution.bookings.size() == problem.exams.size()) break;
			}
		}

		return new EvohypSolution(solution);
	}
}
