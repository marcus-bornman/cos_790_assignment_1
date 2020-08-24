package timetabling.utils;

import timetabling.evohyp.Problem;
import timetabling.domain.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProblemReader {
	/**
	 * Get a problem instance from a file. The file must be in the format specified at the following URL:
	 * http://www.cs.qub.ac.uk/itc2007/examtrack/exam_track_index_files/Inputformat.htm
	 *
	 * @param filePath - the path of the file describing the problem.
	 * @return an instance of the Problem class representing the problem described in filePath.
	 * @throws IOException if no file could be found at the provided path.
	 */
	public static Problem problemFromFile(String filePath) throws IOException {
		String content = Files.readString(Path.of(filePath), StandardCharsets.US_ASCII);
		List<String> sections = Arrays.asList(content.split("\\[.*]\r\n"));

		String name = filePath.substring(filePath.lastIndexOf("/") + 1);
		List<Exam> exams = readExams(sections.get(1));
		List<Period> periods = readPeriods(sections.get(2));
		List<Room> rooms = readRooms(sections.get(3));
		List<PeriodHardConstraint> periodHardConstraints = readPeriodHardConstraints(sections.get(4));
		List<RoomHardConstraint> roomHardConstraints = readRoomHardConstraints(sections.get(5));
		List<InstitutionalWeighting> institutionalWeightings = readInstitutionalWeightings(sections.get(6));

		return new Problem(name, exams, periods, rooms, periodHardConstraints, roomHardConstraints, institutionalWeightings);
	}

	private static List<Exam> readExams(String examSection) {
		List<Exam> exams = new ArrayList<>();

		String[] lines = examSection.split("\r\n");
		for (String line : lines) {
			line = line.replaceAll("\\s+","");
			List<String> parts = Arrays.asList(line.split(","));
			int durationInMinutes = Integer.parseInt(parts.get(0));
			List<String> students = parts.subList(1, parts.size());
			exams.add(new Exam(exams.size(), durationInMinutes, students));
		}

		return exams;
	}

	private static List<Period> readPeriods(String periodSection) {
		List<Period> periods = new ArrayList<>();

		String[] lines = periodSection.split("\r\n");
		for (String line : lines) {
			line = line.replaceAll("\\s+","");
			List<String> parts = Arrays.asList(line.split(","));
			List<String> dateParts = Arrays.asList(parts.get(0).split(":"));
			LocalDate date = LocalDate.of(Integer.parseInt(dateParts.get(2)), Integer.parseInt(dateParts.get(1)), Integer.parseInt(dateParts.get(0)));
			List<String> timeParts = Arrays.asList(parts.get(1).split(":"));
			LocalTime time = LocalTime.of(Integer.parseInt(timeParts.get(0)), Integer.parseInt(timeParts.get(1)), Integer.parseInt(timeParts.get(2)));
			int duration = Integer.parseInt(parts.get(2));
			int penalty = Integer.parseInt(parts.get(3));
			periods.add(new Period(periods.size(), date, time, duration, penalty));
		}

		return periods;
	}

	private static List<Room> readRooms(String roomSection) {
		List<Room> rooms = new ArrayList<>();

		String[] lines = roomSection.split("\r\n");
		for (String line : lines) {
			line = line.replaceAll("\\s+","");
			List<String> parts = Arrays.asList(line.split(","));
			int capacity = Integer.parseInt(parts.get(0));
			int penalty = Integer.parseInt(parts.get(1));
			rooms.add(new Room(rooms.size(), capacity, penalty));
		}

		return rooms;
	}

	private static List<PeriodHardConstraint> readPeriodHardConstraints(String periodHardConstraintSection) {
		List<PeriodHardConstraint> periodHardConstraints = new ArrayList<>();

		String[] lines = periodHardConstraintSection.split("\r\n");
		for (String line : lines) {
			line = line.replaceAll("\\s+","");
			List<String> parts = Arrays.asList(line.split(","));
			int examOneNum = Integer.parseInt(parts.get(0));
			String constraintType = parts.get(1);
			int examTwoNum = Integer.parseInt(parts.get(2));
			periodHardConstraints.add(new PeriodHardConstraint(examOneNum, constraintType, examTwoNum));
		}

		return periodHardConstraints;
	}

	private static List<RoomHardConstraint> readRoomHardConstraints(String roomHardConstraintSection) {
		if (roomHardConstraintSection.isEmpty()) return new ArrayList<>();
		List<RoomHardConstraint> roomHardConstraints = new ArrayList<>();

		String[] lines = roomHardConstraintSection.split("\r\n");
		for (String line : lines) {
			line = line.replaceAll("\\s+","");
			List<String> parts = Arrays.asList(line.split(","));
			int examNum = Integer.parseInt(parts.get(0));
			String constraintType = parts.get(1);
			roomHardConstraints.add(new RoomHardConstraint(examNum, constraintType));
		}

		return roomHardConstraints;
	}

	private static List<InstitutionalWeighting> readInstitutionalWeightings(String institutionalWeightingsSection) {
		List<InstitutionalWeighting> institutionalWeightings = new ArrayList<>();

		String[] lines = institutionalWeightingsSection.split("\r\n");
		for (String line : lines) {
			line = line.replaceAll("\\s+","");
			List<String> parts = Arrays.asList(line.split(","));
			String type = parts.get(0);
			int paramOne = Integer.parseInt(parts.get(1));
			if (!type.equals("FRONTLOAD")) {
				institutionalWeightings.add(new InstitutionalWeighting(type, paramOne));
			} else {
				int paramTwo = Integer.parseInt(parts.get(2));
				int paramThree = Integer.parseInt(parts.get(3));
				institutionalWeightings.add(new InstitutionalWeighting(type, paramOne, paramTwo, paramThree));
			}
		}

		return institutionalWeightings;
	}
}
