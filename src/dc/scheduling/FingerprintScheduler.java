package dc.scheduling;

import java.util.Arrays;
import java.util.Random;
import java.util.InputMismatchException;

import dc.DCPackage;

public class FingerprintScheduler implements Scheduler {
	// The current fingerprint that is used to identify the slot that we reserved
	private int fingerprint;
	// The number of bytes that are used in the schedule for each slot
	private final int bytesPerSlot = 1;
	// The slot that we desire to reserve
	private int desiredSlot;
	// The slot that we <b> succesfully </b> reserved
	private int chosenSlot;
	// The number of slots in the schedule. This equals the size of the scheduling phase.
	private final int numSlots;
	
	// The likelyhood that we withdraw our reservation attempt if we encounter a collision
	// default is 0.5
	private static final double WITHDRAW_CHANCE = 0.5;

	// An instance of random for choice dependent operations
	private final Random random;

	/**
	 * 
	 * @param  numSlots   The number of slots in a schedule. This influences the size of the scheduling block as well as the duration of a scheduling phase.
	 */
	public FingerprintScheduler(int numSlots) {
		this.numSlots = numSlots;
		desiredSlot = (int) (Math.random() * (double) numSlots);
		// System.out.println("[FingerprintScheduler] Attempting to reserve slot \t" + desiredSlot);
		chosenSlot = -1;
		random = new Random();
		refreshFingerprint();
	}

	
	@Override
	public boolean addPackage(DCPackage p) {
		if(p.getNumberRange() != numSlots) {
			throw new InputMismatchException("[FingerprintScheduler] umm... the numberRange is " + p.getNumberRange() + " but the number of slots is " + numSlots+
				". This is not gonna work...");
		}
		byte[] schedule = p.getSchedule(getScheduleSize());
		boolean hasCollision = hasCollision(schedule);
		refreshFingerprint();
		if(p.getNumber() == numSlots -1) {
			// This is the last round of this schedule. If there is no collision then we have found a round for the upcoming phase
			if(!hasCollision) {
				chosenSlot = desiredSlot;
				// System.out.println("[FingerprintScheduler] Succesfully reserved \t" + desiredSlot);
			} else {
				chosenSlot = -1;
				// System.out.println("[FingerprintScheduler] Failed at reserving \t" + desiredSlot);
			}
			// Pick a different slot for the next round
			desiredSlot = (int) (Math.random() * (double) numSlots);
			// System.out.println("[FingerprintScheduler] Attempting to reserve slot \t" + desiredSlot);
			// No matter if we succeeded or not, there is a new round number
			return true;
		} else {
			if(hasCollision) {
				
				if(withdraw()) {
					// We try to move to a different slot.
					// If there are free slots left, then we'll move to one of them.
					// Otherwise pickFree will return -1, indicating that we don't attempt to reserve a slot any longer.
					desiredSlot = pickFree(schedule);
					// System.out.println("[FingerprintScheduler] Moved to free slot \t" + desiredSlot);
				} else {
					// We simply stick to our current slot.
					// System.out.println("[FingerprintScheduler] Sticking to slot \t" + desiredSlot + " although collision");
				}
			} else {
				// If there's no collision then we simply stick to our current desired slot
				// System.out.println("[FingerprintScheduler] Sticking to slot \t" + desiredSlot);
			}
			// Return false since this was not the last round of the current phase.
			return false;
		}
	}

	@Override
	public int getScheduleSize() {
		return numSlots * bytesPerSlot;
	}

	@Override
	public byte[] getSchedule() {
		byte[] schedule = new byte[numSlots * bytesPerSlot];
		if(desiredSlot != -1) {
			setSlot(schedule, desiredSlot, fingerprint);
		}
		return schedule;
	}

	@Override
	public int getNextRound() {
		return chosenSlot;
	}

	/**
	 * Choses a new, randomly generated fingerprint
	 */
	private void refreshFingerprint() {
		fingerprint = random.nextInt(1 << (bytesPerSlot * 8));
	}

	/**
	 * Checks if the given schedule contains scheduling collisions.
	 * In order to check that, we compare the desired slot with the current
	 * fingerprint.
	 * @param  schedule The schedule that is checked for collisions
	 * @return          False if we do not want to reserve a slot, or if the desired slot only contains our current fingerprint.
	 */
	private boolean hasCollision(byte[] schedule) {
		if(desiredSlot == -1) return false;
		int content = extractSlot(schedule, desiredSlot);
		return content != fingerprint;
	}

	/**
	 * Extracts the value of a specific slot of a given schedule.
	 * The value is stored in little-endian manner.
	 * @param  schedule The schedule to extract a value from.
	 * @param  slot     The slot that to be extracted.
	 * @return          The value in the given slot.
	 */
	private int extractSlot(byte[] schedule, int slot) {
		int start = slot * bytesPerSlot;
		int value = 0;
		for(int i = 0; i < bytesPerSlot; i++) {
			// Shift value over to make room for the next byte
			// Initially (value == 0), this operation has no effect.
			value <<= 8;
			value += (int) schedule[start + i] & 0xff;
		}
		return value;
	}

	/**
	 * Stores a specific value in a schedule
	 * @param schedule The schedule to store the value in.
	 * @param slot     The index of the slot in which the value will be stored.
	 * @param value    The value to be stored.
	 */
	private void setSlot(byte[] schedule, int slot, int value) {
		int start = slot * bytesPerSlot;
		for(int i = 0; i < bytesPerSlot; i++) {
			schedule[start + i] = (byte) (value % 256);
			// Shift value over to expose the next byte
			value >>= 8;
		}
	}

	/**
	 * Picks a random free slot in a given schedule
	 * @param  schedule The schedule to be scanned
	 * @return          The index of a random free slot, or -1 if there are no free slots.
	 */
	private int pickFree(byte[] schedule) {
		int[] freeSlots = new int[numSlots];
		int numFrees = 0;
		for(int i = 0; i < numSlots; i++) {
			if(extractSlot(schedule, i) == 0) {
				freeSlots[numFrees] = i;
				numFrees++;
			}
		}
		if(numFrees == 0) {
			return -1;
		} else {
			int i = random.nextInt(numFrees);
			return freeSlots[i];
		}
	}

	/**
	 * Returns whether or not to withdraw the reservation
	 * attempt. The outcome of this function depends on the value
	 * WITHDRAW_CHANCE. 
	 */
	private boolean withdraw() {
		double value = random.nextDouble();
		return value < WITHDRAW_CHANCE;
	}

}