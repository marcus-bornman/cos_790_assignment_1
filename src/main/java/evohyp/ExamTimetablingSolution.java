package evohyp;

import initialsoln.InitialSoln;
import domain.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ExamTimetablingSolution extends InitialSoln {
	private String heuristicComb;

	private final ExamTimetablingProblem problem;

	public final List<Booking> bookings;

	private final double fitness;


	/**
	 * @param problem - the problem that this solution will solve.
	 */
	public ExamTimetablingSolution(ExamTimetablingProblem problem, List<Booking> bookings) {
		this.problem = problem;
		this.bookings = bookings;
		this.fitness = calcFitness();
	}

	@Override
	public int fitter(InitialSoln other) {
		return Double.compare(other.getFitness(), getFitness());
	}

	@Override
	public double getFitness() {
		return fitness;
	}

	@Override
	public Object getSoln() {
		return bookings;
	}

	@Override
	public String solnToString() {
		StringBuilder solnString = new StringBuilder();
		for (Booking booking : bookings) {
			solnString.append("\n").append(booking);
		}

		return solnString.toString();
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
	 * @return a double value representing the fitness of the solution.
	 */
	private double calcFitness() {
		double fitness = 0;

		// Hard constraints
		fitness += 100 * calcUnbookedExams();
		fitness += 100 * calcConflictingExams();
		fitness += 100 * calcOverbookedPeriods();
		fitness += 100 * calcTooShortPeriods();
		fitness += 100 * calcPeriodConstraintViolations();
		fitness += 100 * calcRoomConstraintViolations();

		// Soft constraints
		InstitutionalWeighting twoInARowWeighting = problem.institutionalWeightings.stream().filter(w -> w.weightingType.equals("TWOINAROW")).findFirst().orElse(null);
		if (twoInARowWeighting != null) fitness += calcTwoInARowPenalty(twoInARowWeighting);
		InstitutionalWeighting twoInADayWeighting = problem.institutionalWeightings.stream().filter(w -> w.weightingType.equals("TWOINADAY")).findFirst().orElse(null);
		if (twoInADayWeighting != null) fitness += calcTwoInADayPenalty(twoInADayWeighting);
		InstitutionalWeighting periodSpreadWeighting = problem.institutionalWeightings.stream().filter(w -> w.weightingType.equals("PERIOD_SPREAD")).findFirst().orElse(null);
		if (periodSpreadWeighting != null) fitness += calcPeriodSpreadPenalty(periodSpreadWeighting);
		InstitutionalWeighting mixedDurationsWeighting = problem.institutionalWeightings.stream().filter(w -> w.weightingType.equals("NONMIXEDDURATIONS")).findFirst().orElse(null);
		if (mixedDurationsWeighting != null) fitness += calcMixedDurationsPenalty(mixedDurationsWeighting);
		InstitutionalWeighting frontloadWeighting = problem.institutionalWeightings.stream().filter(w -> w.weightingType.equals("FRONTLOAD")).findFirst().orElse(null);
		if (frontloadWeighting != null) fitness += calcFrontloadPenalty(frontloadWeighting);
		fitness += calcRoomPenalty();
		fitness += calcPeriodPenalty();

		return fitness;
	}

	private int calcUnbookedExams() {
		int unbookedExams = 0;

		for (Exam exam : problem.exams) {
			boolean isNotScheduled = bookings.stream().noneMatch(b -> b.exam.number == exam.number);
			if (isNotScheduled) unbookedExams++;
		}

		return unbookedExams;
	}

	private int calcConflictingExams() {
		int conflictingExams = 0;

		for (Booking bookingA : bookings) {
			for (Booking bookingB : bookings) {
				if (bookingA.equals(bookingB)) continue;
				boolean doClash = bookingA.period.number == bookingB.period.number;
				boolean doShareStudents = problem.clashMatrix[bookingA.exam.number][bookingB.exam.number] > 0;
				if (doClash && doShareStudents) conflictingExams++;
			}
		}

		return conflictingExams;
	}

	private int calcOverbookedPeriods() {
		int overbookedPeriods = 0;

		for (Period period : problem.periods) {
			for (Room room : problem.rooms) {
				List<Booking> periodRoomBookings = bookings.stream().filter(b -> b.period.number == period.number && b.room.number == room.number).collect(Collectors.toList());
				int numSeatsNeeded = periodRoomBookings.stream().mapToInt(b -> b.exam.students.size()).sum();
				if (numSeatsNeeded > room.capacity) overbookedPeriods++;
			}
		}

		return overbookedPeriods;
	}

	private int calcTooShortPeriods() {
		int tooShortPeriods = 0;

		HashSet<Booking> seen = new HashSet<>();
		for (Booking booking : bookings) {
			if (booking.exam.duration > booking.period.duration && seen.add(booking)) tooShortPeriods++;
		}

		return tooShortPeriods;
	}

	private int calcPeriodConstraintViolations() {
		int violations = 0;

		for (PeriodHardConstraint constraint : problem.periodHardConstraints) {
			Booking bookingOne = bookings.stream().filter(b -> b.exam.number == constraint.examOneNum).findFirst().orElse(null);
			Booking bookingTwo = bookings.stream().filter(b -> b.exam.number == constraint.examTwoNum).findFirst().orElse(null);
			if (bookingOne == null || bookingTwo == null) continue;

			if (constraint.constraintType.equals("EXAM_COINCIDENCE")) {
				if (problem.clashMatrix[constraint.examOneNum][constraint.examTwoNum] > 0) continue;
				if (bookingOne.period.number != bookingTwo.period.number) violations++;
			}

			if (constraint.constraintType.equals("EXCLUSION")) {
				if (bookingOne.period.number == bookingTwo.period.number) violations++;
			}

			if (constraint.constraintType.equals("AFTER")) {
				if (bookingOne.period.getDateTime().isAfter(bookingTwo.period.getDateTime())) violations++;
			}
		}

		return violations;
	}

	private int calcRoomConstraintViolations() {
		int violations = 0;

		for (RoomHardConstraint constraint : problem.roomHardConstraints) {
			if (constraint.constraintType.equals("ROOM_EXCLUSIVE")) {
				Booking booking = bookings.stream().filter(b -> b.exam.number == constraint.examNum).findFirst().orElse(null);
				if (booking == null) continue;
				boolean isNotBookedAlone = bookings.stream().anyMatch(b -> b.room.number == booking.room.number && b.period.number == booking.period.number);
				if (isNotBookedAlone) violations++;
			}
		}

		return violations;
	}

	private int calcTwoInARowPenalty(InstitutionalWeighting weighting) {
		int penalty = 0;

		for (Booking bookingA : bookings) {
			for (Booking bookingB : bookings) {
				boolean areInARow = Math.abs(bookingA.period.number - bookingB.period.number) == 1;
				boolean areOnSameDay = bookingA.period.date.isEqual(bookingB.period.date);
				if (areInARow && areOnSameDay)
					penalty += weighting.paramOne * problem.clashMatrix[bookingA.exam.number][bookingB.exam.number];
			}
		}

		return penalty;
	}

	private int calcTwoInADayPenalty(InstitutionalWeighting weighting) {
		int penalty = 0;

		for (Booking bookingA : bookings) {
			for (Booking bookingB : bookings) {
				if (bookingA.equals(bookingB)) continue;
				boolean areNotAdjacent = Math.abs(bookingA.period.number - bookingB.period.number) != 1;
				boolean areOnSameDay = bookingA.period.date.isEqual(bookingB.period.date);
				if (areNotAdjacent && areOnSameDay)
					penalty += weighting.paramOne * problem.clashMatrix[bookingA.exam.number][bookingB.exam.number];
			}
		}

		return penalty;
	}

	private double calcPeriodSpreadPenalty(InstitutionalWeighting weighting) {
		int penalty = 0;

		for (Booking bookingA : bookings) {
			for (Booking bookingB : bookings) {
				int spread = bookingB.period.number - bookingA.period.number;
				if (spread <= 0) continue;
				boolean areWithinSpread = spread <= weighting.paramOne;
				if (areWithinSpread)
					penalty += problem.clashMatrix[bookingA.exam.number][bookingB.exam.number];
			}
		}

		return penalty;
	}

	private double calcMixedDurationsPenalty(InstitutionalWeighting weighting) {
		int penalty = 0;

		for (Period period : problem.periods) {
			for (Room room : problem.rooms) {
				List<Booking> myBookings = bookings.stream().filter(b -> b.period.number == period.number && b.room.number == room.number).collect(Collectors.toList());
				if (myBookings.isEmpty()) continue;
				HashSet<Object> seen = new HashSet<>();
				myBookings.removeIf(b -> !seen.add(b.exam.duration));
				int numDifferentDurations = myBookings.size();
				penalty += (numDifferentDurations - 1) * weighting.paramOne;
			}
		}

		return penalty;
	}

	private double calcFrontloadPenalty(InstitutionalWeighting weighting) {
		int penalty = 0;

		List<Exam> largestExams = problem.exams.stream()
				.sorted((e1, e2) -> e2.students.size() - e1.students.size())
				.collect(Collectors.toList()).subList(0, weighting.paramOne);

		int lastPeriodIndex = problem.periods.size() - weighting.paramTwo;
		if (lastPeriodIndex < 0) lastPeriodIndex = 0;
		List<Period> lastPeriods = problem.periods.subList(lastPeriodIndex, problem.periods.size());

		for (Exam exam : largestExams) {
			Booking examBooking = bookings.stream().filter(b -> b.exam.number == exam.number).findFirst().orElse(null);
			if (examBooking == null) continue;
			boolean isInLastPeriods = lastPeriods.stream().anyMatch(p -> p.number == examBooking.period.number);
			if (isInLastPeriods) penalty += weighting.paramThree;
		}

		return penalty;
	}

	private double calcRoomPenalty() {
		int penalty = 0;

		for (Booking booking : bookings) {
			penalty += booking.room.penalty;
		}

		return penalty;
	}

	private double calcPeriodPenalty() {
		int penalty = 0;

		for (Booking booking : bookings) {
			penalty += booking.period.penalty;
		}

		return penalty;
	}
}
