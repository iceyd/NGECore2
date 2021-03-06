/*******************************************************************************
 * Copyright (c) 2013 <Project SWG>
 * 
 * This File is part of NGECore2.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Using NGEngine to work with NGECore2 is making a combined work based on NGEngine. 
 * Therefore all terms and conditions of the GNU Lesser General Public License cover the combination.
 ******************************************************************************/
package resources.objects.mission;

import java.nio.ByteOrder;

import org.apache.mina.core.buffer.IoBuffer;

import engine.resources.common.CRC;
import resources.common.Console;
import resources.common.StringUtilities;
import resources.objects.ObjectMessageBuilder;
import resources.objects.waypoint.WaypointObject;

public class MissionMessageBuilder extends ObjectMessageBuilder {

	public MissionMessageBuilder(MissionObject missionObject) {
		setObject(missionObject);
	}

	public IoBuffer buildBaseline3() {
		MissionObject mission = (MissionObject) object;
		IoBuffer buffer = IoBuffer.allocate(100, false).order(ByteOrder.LITTLE_ENDIAN);
		buffer.setAutoExpand(true);
		
		buffer.putShort((short) 17);
		
		buffer.putFloat(1); // mission complexity ??
		buffer.put(getAsciiString(mission.getStfFilename()));
		buffer.putInt(0);
		buffer.put(getAsciiString(mission.getStfName()));
		
		if (mission.getCustomName() == null) { buffer.put(getUnicodeString("")); } 
		else { buffer.put(getUnicodeString(mission.getCustomName())); }

		buffer.putInt(0); // volume
		buffer.putInt(0); // unused
		buffer.putInt(mission.getMissionLevel());
		
		buffer.putFloat(mission.getMissionStartX());
		buffer.putFloat(0); // missionStartZ
		buffer.putFloat(mission.getMissionStartY());
		
		buffer.putLong(0); // ??
		
		if (mission.getMissionStartPlanet() == null) {
			buffer.putInt(0);
		} else {
			buffer.putInt(CRC.StringtoCRC(mission.getMissionStartPlanet()));
		}
	
		buffer.put(getUnicodeString(mission.getMissionCreator()));
		buffer.putInt(mission.getMissionCredits());
		
		buffer.putFloat(mission.getMissionDestinationX());
		buffer.putFloat(0); // missionDestinationZ
		buffer.putFloat(mission.getMissionDestinationY());
		
		buffer.putLong(0); // ??
		
		if (mission.getMissionDestinationPlanet() == null) {
			buffer.putInt(0);
		} else {
			buffer.putInt(CRC.StringtoCRC(mission.getMissionDestinationPlanet()));
		}
		
		if (mission.getTargetTemplateObject() == null) {
			buffer.putInt(0);
		} else {
			buffer.putInt(CRC.StringtoCRC(mission.getTargetTemplateObject()));
		}

		buffer.put(getAsciiString(mission.getMissionDescription()));
		buffer.putInt(0);
		buffer.put(getAsciiString(mission.getMissionDescId()));
		
		buffer.put(getAsciiString(mission.getMissionTitle()));
		buffer.putInt(0);
		buffer.put(getAsciiString(mission.getMissionTitleId()));
		
		buffer.putInt(0); // refresh counter
		
		if(mission.getMissionType() == null) {
			buffer.putInt(0);
		} else {
			buffer.putInt(CRC.StringtoCRC(mission.getMissionType()));
		}

		buffer.put(getAsciiString(mission.getMissionTargetName()));
		
		WaypointObject wp = mission.getMissionAttachedWaypoint();
		
		if (wp == null) {
			buffer.putInt(0);
			buffer.putFloat(0); // x
			buffer.putFloat(0); // z
			buffer.putFloat(0); // y
			buffer.putLong(0); // target id
			buffer.putInt(0); // planet crc
			buffer.put(getUnicodeString(""));
			buffer.putLong(0); // waypoint id
			buffer.put((byte) 0); // color
			buffer.put((byte) 0x01); //active
		} else {
			buffer.putInt(wp.getCellId());
			buffer.putFloat(wp.getPosition().x);
			buffer.putFloat(wp.getPosition().z);
			buffer.putFloat(wp.getPosition().y);
			buffer.putLong(0);
			buffer.putInt(CRC.StringtoCRC(wp.getPlanet().name));
			buffer.put((byte) 0);
			buffer.put((byte) 0x01);
		}
		int size = buffer.position();
		buffer = bufferPool.allocate(size, false).put(buffer.array(), 0, size);
		
		buffer.flip();
		buffer = createBaseline("MISO", (byte) 3, buffer, size);
		
		Console.println("Bytes: " + StringUtilities.bytesToHex(buffer.array()));
		return buffer;
	}
	
	public IoBuffer buildBaseline6() {
		
		IoBuffer buffer = IoBuffer.allocate(4, false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putInt(121); // unk
		
		int size = buffer.position();
		buffer.flip();
		buffer = createBaseline("MISO", (byte) 6, buffer, size);
		
		return buffer;
	}
	
	public IoBuffer buildStfDelta(String stfFile, String stfName) {
		
		IoBuffer buffer = IoBuffer.allocate(4 + stfFile.length() + stfName.length(), false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.put(getAsciiString(stfFile));
		buffer.putInt(0);
		buffer.put(getAsciiString(stfName));
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x01, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildDifficultyLevelDelta(int difficulty) {
		
		IoBuffer buffer = IoBuffer.allocate(4, false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putInt(difficulty);
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x05, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildStartLocationDelta(float x, float z, float y, String planet) {

		IoBuffer buffer = IoBuffer.allocate(24, false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putFloat(x);
		buffer.putFloat(z);
		buffer.putFloat(y);

		buffer.putLong(0);
		
		buffer.putInt(CRC.StringtoCRC(planet));
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x06, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildCreatorNameDelta(String creator) {

		IoBuffer buffer = IoBuffer.allocate(4 + creator.length(), false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.put(getAsciiString(creator));
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x07, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildCreditsRewardDelta(int creds) {

		IoBuffer buffer = IoBuffer.allocate(4, false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putInt(creds);
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x08, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildDestinationDelta(float x, float z, float y, String planet) {

		IoBuffer buffer = IoBuffer.allocate(24, false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putFloat(x);
		buffer.putFloat(z);
		buffer.putFloat(y);
		
		buffer.putLong(0); // Destination Object ID
		
		buffer.putInt(CRC.StringtoCRC(planet));
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x09, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildTargetObjectIffDelta(String template) {

		IoBuffer buffer = IoBuffer.allocate(4, false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putInt(CRC.StringtoCRC(template));
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x0A, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildMissionDescriptionDelta(String desc, String id) {
		
		IoBuffer buffer = IoBuffer.allocate(8 + desc.length() + id.length(), false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.put(getAsciiString(desc));
		buffer.putInt(0);
		buffer.put(getAsciiString(id));
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x0B, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildMissionTitleDelta(String title, String id) {

		IoBuffer buffer = IoBuffer.allocate(8 + title.length() + id.length(), false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.put(getAsciiString(title));
		buffer.putInt(0);
		buffer.put(getAsciiString(id));
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x0C, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildRepeatCounterDelta(int counter) {
		
		IoBuffer buffer = IoBuffer.allocate(4, false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putInt(counter);
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x0D, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildMissionTypeDelta(String type) {

		IoBuffer buffer = IoBuffer.allocate(4, false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.putInt(CRC.StringtoCRC(type));
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x0E, buffer, size + 4);
		
		return buffer;
	}
	
	public IoBuffer buildTargetNameDelta(String targetName) {
		
		IoBuffer buffer = IoBuffer.allocate(4 + targetName.length(), false).order(ByteOrder.LITTLE_ENDIAN);
		
		buffer.put(getAsciiString(targetName));
		
		int size = buffer.position();
		buffer.flip();
		buffer = createDelta("MISO", (byte) 3, (short) 1, (short) 0x0F, buffer, size + 4);
		
		return buffer;
	}

	@Override
	public void sendListDelta(byte viewType, short updateType, IoBuffer buffer) {
		
	}

	@Override
	public void sendBaselines() {
		
	}

}
