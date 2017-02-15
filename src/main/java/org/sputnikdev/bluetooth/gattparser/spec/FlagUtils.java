package org.sputnikdev.bluetooth.gattparser.spec;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sputnikdev.bluetooth.gattparser.BluetoothGattParserFactory;
import org.sputnikdev.bluetooth.gattparser.num.RealNumberFormatter;

public class FlagUtils {

    private final static RealNumberFormatter realNumberFormatter =
            BluetoothGattParserFactory.getTwosComplementNumberFormatter();

    private FlagUtils() { }

    public static Set<String> getAllReadFlags(Field flagsField) {
        Set<String> result = new HashSet<>();
        if (flagsField != null && flagsField.getBitField() != null) {
            for (Bit bit : flagsField.getBitField().getBits()) {
                for (Enumeration enumeration : bit.getEnumerations().getEnumerations()) {
                    if (enumeration.getRequires() != null) {
                        result.add(enumeration.getRequires());
                    }
                }
            }
        }
        return result;
    }

    public static Set<String> getAllWriteFlags(Field field) {
        Set<String> result = new HashSet<>();
        if (field.getEnumerations() == null || field.getEnumerations().getEnumerations() == null) {
            return Collections.EMPTY_SET;
        }
        for (Enumeration enumeration : field.getEnumerations().getEnumerations()) {
            result.add(enumeration.getRequires());
        }
        return result;
    }

    public static Set<String> getReadFlags(Field flagsField, byte[] data) {
        Set<String> flags = new HashSet<>();
        if (flagsField != null && flagsField.getBitField() != null) {
            int[] values = parseReadFlags(flagsField, data);
            int i = 0;
            for (Bit bit : flagsField.getBitField().getBits()) {
                String value = bit.getFlag((byte) values[i++]);
                if (value != null) {
                    flags.add(value);
                }
            }
        }
        return flags;
    }

    public static String getWriteFlag(Field field, Integer key) {
        if (field.getEnumerations() == null || field.getEnumerations().getEnumerations() == null) {
            return null;
        }

        if (key == null) {
            return null;
        }

        for (Enumeration enumeration : field.getEnumerations().getEnumerations()) {
            if (key.equals(enumeration.getKey())) {
                return enumeration.getRequires();
            }
        }
        return null;
    }


    static int[] parseReadFlags(Field flagsField, byte[] raw) {
        BitSet bitSet = BitSet.valueOf(raw).get(0, flagsField.getFormat().getSize());
        List<Bit> bits = flagsField.getBitField().getBits();
        int[] flags = new int[bits.size()];
        int offset = 0;
        for (int i = 0; i < bits.size(); i++) {
            int size = bits.get(i).getSize();
            flags[i] = realNumberFormatter.deserializeInteger(bitSet.get(offset, offset + size), size, false);
            offset += size;
        }
        return flags;
    }

}