package io.bce.interaction.streaming.binary

import spock.lang.Specification

class BinaryChunkSpec extends Specification {
	def "Scenario: create binary chunk"() {
		given: "The binary chunk is created for bytes array ${array}"
		BinaryChunk chunk = new BinaryChunk((byte[]) array)
		
		expect: "The chunk body is ${array}"
		Arrays.equals(chunk.getBody(), (byte[]) array)
		
		and: "The chunk size is: ${size}"
		chunk.getSize() == size
		
		and: "The chunk empty flag is: ${isEmpty}"
		chunk.isEmpty() == isEmpty
		
		where: 
		array                 | size               | isEmpty
		[]                    | 0                  | true
		[1]                   | 1                  | false
		[1, 2]                | 2                  | false
		[1, 2, 3]             | 3                  | false
	}
	
	def "Scenario: empty binary chunk"() {
		expect: "The empty binary chunk is binary chunk created for empty array"
		BinaryChunk.EMPTY == new BinaryChunk((byte[]) [])
	}
}
