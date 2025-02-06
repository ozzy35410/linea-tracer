/*
 * Copyright Consensys Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.consensys.linea.zktracer.returndata;

import net.consensys.linea.testing.BytecodeCompiler;
import net.consensys.linea.zktracer.opcode.OpCode;

import static com.google.common.base.Preconditions.checkArgument;
import static net.consensys.linea.zktracer.opcode.OpCode.*;

public class Helpers {

    /**
     * Simple byte code which, when called, returns 64B's of return data obtained by concatenating
     * the two 32B strings composed exclusive of repetitions of the first and second characters respectively.
     * @param char1 must be hex character
     * @param char2 must be hex character
     * @param opCode must be either {@link OpCode#RETURN} or {@link OpCode#REVERT}
     * @return
     */
    public BytecodeCompiler simpleReturnDataProducer(OpCode opCode, char char1, char char2) {


        BytecodeCompiler program = BytecodeCompiler.newProgram();

        writeMonoCharacterWordToMemory(program, 0, char1);
        writeMonoCharacterWordToMemory(program, 1, char2);
        provideFullMemoryAsReturnData(program, opCode);

        return program;
    }

    private boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
    }

    /**
     * {@link #polluteAlignedMemoryWord(BytecodeCompiler, int)} writes some gibberish to an (aligned) word in memory.
     * @param program
     * @param wordOffset
     */
    public void polluteAlignedMemoryWord(BytecodeCompiler program, int wordOffset) {

        int byteOffset = 32 * wordOffset;
        int valueToHash = wordOffset + 1;
        program
                .push(valueToHash)
                .push(byteOffset)
                .op(MSTORE)
                .push(32) // size
                .push(byteOffset) // offset
                .op(SHA3)
                .push(byteOffset)
                .op(MSTORE)
                ;
    }

    public void writeMonoCharacterWordToMemory(BytecodeCompiler program, int wordOffset, char hexCharToRepeat32Times) {
        checkArgument(isHexChar(hexCharToRepeat32Times));
        int byteOffset = 32 * wordOffset;
        program
                .push(String.valueOf(hexCharToRepeat32Times).repeat(64))
                .push(byteOffset)
                .op(MSTORE)
                ;
    }

    public void resetWordInMemory(BytecodeCompiler program, int wordOffset) {
        writeMonoCharacterWordToMemory(program, wordOffset, '0');
    }

    public void provideFullMemoryAsReturnData(BytecodeCompiler program, OpCode opCode) {
        checkArgument(opCode == RETURN || opCode == REVERT);
        program
                .op(MSIZE) // size
                .push(0) // offset
                .op(opCode)
                ;
    }
}
