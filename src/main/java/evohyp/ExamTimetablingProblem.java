package evohyp;

import problemdomain.ProblemDomain;
import domain.*;

import java.util.List;

public class ExamTimetablingProblem extends ProblemDomain {
	public final List<Exam> exams;
	public final List<Period> periods;
	public final List<Room> rooms;
	public final List<PeriodHardConstraint> periodHardConstraints;
	public final List<RoomHardConstraint> roomHardConstraints;
	public final List<InstitutionalWeighting> institutionalWeightings;
	public final int[][] clashMatrix;

	/**
	 * The constructor for the Problem class.
	 *
	 * @param exams                   - The exams that need to be scheduled.
	 * @param periods                 - The periods in which exams can be scheduled.
	 * @param rooms                   - The rooms in which exams can be scheduled.
	 * @param periodHardConstraints   - The set of hard constraints for periods.
	 * @param roomHardConstraints     - The set of hard constraints for rooms.
	 * @param institutionalWeightings - The set of institutional weightings which serve as soft constraints.
	 */
	public ExamTimetablingProblem(List<Exam> exams, List<Period> periods, List<Room> rooms, List<PeriodHardConstraint> periodHardConstraints, List<RoomHardConstraint> roomHardConstraints, List<InstitutionalWeighting> institutionalWeightings) {
		this.exams = exams;
		this.periods = periods;
		this.rooms = rooms;
		this.periodHardConstraints = periodHardConstraints;
		this.roomHardConstraints = roomHardConstraints;
		this.institutionalWeightings = institutionalWeightings;
		this.clashMatrix = new int[exams.size()][exams.size()];
		for (int i = 0; i < exams.size(); i++) {
			for (int j = 0; j < exams.size(); j++) {
				Exam examOne = exams.get(i);
				Exam examTwo = exams.get(j);
				int numClashes = (int) examOne.students.stream().filter(s1 -> examTwo.students.stream().anyMatch(s1::equals)).count();
				this.clashMatrix[i][j] = numClashes;
			}
		}
	}

	/**
	 * This method takes the heuristic combination heuristicComb evolved by
	 * the genetic algorithm in the package GenAlg or DistrGenAlg as input. The
	 * string specified in heuristicComb is used to construct a solution to the
	 * problem and returns an object of type InitialSoln. An instance of the type
	 * InitialSoln combines the objective value or function of the objective value
	 * that will be used as a fitness value for each chromosome by the genetic
	 * algorithm and the solution created using the evolved string of heuristics.
	 *
	 * @param heuristicComb - the heuristic combination evolved by the genetic
	 *                      algorithm in the package GenAlg or DistrGenAlg.
	 * @return an object of type InitialSoln.
	 */
	@Override
	public ExamTimetablingSolution evaluate(String heuristicComb) {
		ExamTimetablingSolution solution = new ExamTimetablingSolution(this, List.of());

		for (char character : heuristicComb.toCharArray()) {
			ConstructiveHeuristicEngine constructiveHeuristicEngine = new ConstructiveHeuristicEngine(this, character);
			for (int i = 0; i < 5; i++) {
				solution = constructiveHeuristicEngine.applyToSolution(solution);
			}
		}

		return solution;
	}
}
