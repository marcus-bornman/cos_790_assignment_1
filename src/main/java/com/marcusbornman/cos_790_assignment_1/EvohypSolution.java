package com.marcusbornman.cos_790_assignment_1;

import initialsoln.InitialSoln;
import dev.born.itc2007.ExamTimetablingSolution;

public class EvohypSolution extends InitialSoln {
	private String heuristicComb;

	private Double fitness;

	private final ExamTimetablingSolution solution;

	/**
	 * @param solution - the problem that this solution will solve.
	 */
	public EvohypSolution(ExamTimetablingSolution solution) {
		this.solution = solution;
	}

	@Override
	public int fitter(InitialSoln other) {
		return Double.compare(other.getFitness(), getFitness());
	}

	@Override
	public double getFitness() {
		if (fitness == null) fitness = calcFitness();
		return fitness;
	}

	@Override
	public Object getSoln() {
		return solution;
	}

	@Override
	public String solnToString() {
		return solution.toString();
	}

	@Override
	public void setHeuCom(String heuristicComb) {
		this.heuristicComb = heuristicComb;
	}

	@Override
	public String getHeuCom() {
		return heuristicComb;
	}

	/**
	 * A method to calculate the fitness of this solution. Details as to how fitness is evaluated can be found at
	 * http://www.cs.qub.ac.uk/itc2007/examtrack/exam_track_index_files/examevaluation.htm
	 *
	 * Since this is in the context of a constructive heuristic, only the hard constraints (distance to feasibility)
	 * are considered for the fitness of solutions in the pre-feasibility space.
	 *
	 * @return a double value representing the fitness of the solution.
	 */
	private double calcFitness() {
		int hardConstraintViolations = solution.conflictingExams() + solution.overbookedPeriods() + solution.tooShortPeriods() +
				solution.periodConstraintViolations() + solution.roomConstraintViolations();

		int softConstraintPenalties = solution.twoInARowPenalty() + solution.twoInADayPenalty() + solution.periodSpreadPenalty() +
				solution.mixedDurationsPenalty() + solution.periodSpreadPenalty() + solution.roomPenalty() + solution.periodPenalty();

		return softConstraintPenalties * (hardConstraintViolations + 1);
	}
}
