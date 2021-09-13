package net.runelite.cache.definitions.loaders;

import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.io.InputStream;

@Slf4j
public class ModelLoader
{
	public ModelDefinition load(int modelId, byte[] b)
	{
		ModelDefinition def = new ModelDefinition();
		def.id = modelId;

		if (b[b.length - 1] == -1 && b[b.length - 2] == -1)
		{
			log.debug("Model {} with load1", modelId);
			this.load1(def, b);
		}
		else
		{
			log.debug("Model {} with load2", modelId);
			this.load2(def, b);
		}

		def.computeNormals();
		def.computeTextureUVCoordinates();
		def.computeAnimationTables();

		return def;
	}

	private void load1(ModelDefinition model, byte[] var1)
	{
		InputStream var2 = new InputStream(var1);
		InputStream var24 = new InputStream(var1);
		InputStream var3 = new InputStream(var1);
		InputStream var28 = new InputStream(var1);
		InputStream var6 = new InputStream(var1);
		InputStream var55 = new InputStream(var1);
		InputStream var51 = new InputStream(var1);
		var2.setOffset(var1.length - 23);
		int vertexCount = var2.readUnsignedShort();
		int faceCount = var2.readUnsignedShort();
		int textureTriangleCount = var2.readUnsignedByte();
		int hasFaceRenderTypes = var2.readUnsignedByte();
		int modelPriority = var2.readUnsignedByte();
		int hasFaceAlphas = var2.readUnsignedByte();
		int hasFaceSkins = var2.readUnsignedByte();
		int modelTexture = var2.readUnsignedByte();
		int modelVertexSkins = var2.readUnsignedByte();
		int xVertexCount = var2.readUnsignedShort();
		int yVertexCount = var2.readUnsignedShort();
		int zVertexCount = var2.readUnsignedShort();
		int indicesCount = var2.readUnsignedShort();
		int var38 = var2.readUnsignedShort();
		int textureAmount = 0;
		int var7 = 0;
		int var29 = 0;
		int position;
		if (textureTriangleCount > 0)
		{
			model.textureRenderTypes = new byte[textureTriangleCount];
			var2.setOffset(0);

			for (position = 0; position < textureTriangleCount; ++position)
			{
				byte renderType = model.textureRenderTypes[position] = var2.readByte();
				if (renderType == 0)
				{
					++textureAmount;
				}

				if (renderType >= 1 && renderType <= 3)
				{
					++var7;
				}

				if (renderType == 2)
				{
					++var29;
				}
			}
		}

		position = textureTriangleCount + vertexCount;
		int renderTypePos = position;
		if (hasFaceRenderTypes == 1)
		{
			position += faceCount;
		}

		int var49 = position;
		position += faceCount;
		int priorityPos = position;
		if (modelPriority == 255)
		{
			position += faceCount;
		}

		int triangleSkinPos = position;
		if (hasFaceSkins == 1)
		{
			position += faceCount;
		}

		int var35 = position;
		if (modelVertexSkins == 1)
		{
			position += vertexCount;
		}

		int alphaPos = position;
		if (hasFaceAlphas == 1)
		{
			position += faceCount;
		}

		int var11 = position;
		position += indicesCount;
		int texturePos = position;
		if (modelTexture == 1)
		{
			position += faceCount * 2;
		}

		int textureCoordPos = position;
		position += var38;
		int colorPos = position;
		position += faceCount * 2;
		int vertexXPosition = position;
		position += xVertexCount;
		int vertexYPosition = position;
		position += yVertexCount;
		int vertexZPosition = position;
		position += zVertexCount;
		int var43 = position;
		position += textureAmount * 6;
		int var37 = position;
		position += var7 * 6;
		int var48 = position;
		position += var7 * 6;
		int var56 = position;
		position += var7 * 2;
		int var45 = position;
		position += var7;
		int var46 = position;
		position += var7 * 2 + var29 * 2;
		model.vertexCount = vertexCount;
		model.faceCount = faceCount;
		model.textureTriangleCount = textureTriangleCount;
		model.vertexPositionsX = new int[vertexCount];
		model.vertexPositionsY = new int[vertexCount];
		model.vertexPositionsZ = new int[vertexCount];
		model.faceVertexIndices1 = new int[faceCount];
		model.faceVertexIndices2 = new int[faceCount];
		model.faceVertexIndices3 = new int[faceCount];
		if (modelVertexSkins == 1)
		{
			model.vertexSkins = new int[vertexCount];
		}

		if (hasFaceRenderTypes == 1)
		{
			model.faceRenderTypes = new byte[faceCount];
		}

		if (modelPriority == 255)
		{
			model.faceRenderPriorities = new byte[faceCount];
		}
		else
		{
			model.priority = (byte) modelPriority;
		}

		if (hasFaceAlphas == 1)
		{
			model.faceAlphas = new byte[faceCount];
		}

		if (hasFaceSkins == 1)
		{
			model.faceSkins = new int[faceCount];
		}

		if (modelTexture == 1)
		{
			model.faceTextures = new short[faceCount];
		}

		if (modelTexture == 1 && textureTriangleCount > 0)
		{
			model.textureCoordinates = new byte[faceCount];
		}

		model.faceColors = new short[faceCount];
		if (textureTriangleCount > 0)
		{
			model.textureTriangleVertexIndices1 = new short[textureTriangleCount];
			model.textureTriangleVertexIndices2 = new short[textureTriangleCount];
			model.textureTriangleVertexIndices3 = new short[textureTriangleCount];
			if (var7 > 0)
			{
				model.aShortArray2574 = new short[var7];
				model.aShortArray2575 = new short[var7];
				model.aShortArray2586 = new short[var7];
				model.aShortArray2577 = new short[var7];
				model.aByteArray2580 = new byte[var7];
				model.aShortArray2578 = new short[var7];
			}

			if (var29 > 0)
			{
				model.texturePrimaryColors = new short[var29];
			}
		}

		var2.setOffset(textureTriangleCount);
		var24.setOffset(vertexXPosition);
		var3.setOffset(vertexYPosition);
		var28.setOffset(vertexZPosition);
		var6.setOffset(var35);
		int lastVertexX = 0;
		int lastVertexY = 0;
		int lastVertexZ = 0;

		int point;
		for (point = 0; point < vertexCount; ++point)
		{
			int preX = lastVertexX;
			int preY = lastVertexY;
			int preZ = lastVertexZ;

			int vertexFlags = var2.readUnsignedByte();
			int vertexXOffset = 0;
			if ((vertexFlags & 1) != 0)
			{
				vertexXOffset = var24.readShortSmart();
			}

			int vertexYOffset = 0;
			if ((vertexFlags & 2) != 0)
			{
				vertexYOffset = var3.readShortSmart();
			}

			int vertexZOffset = 0;
			if ((vertexFlags & 4) != 0)
			{
				vertexZOffset = var28.readShortSmart();
			}

			model.vertexPositionsX[point] = lastVertexX + vertexXOffset;
			model.vertexPositionsY[point] = lastVertexY + vertexYOffset;
			model.vertexPositionsZ[point] = lastVertexZ + vertexZOffset;
			lastVertexX = model.vertexPositionsX[point];
			lastVertexY = model.vertexPositionsY[point];
			lastVertexZ = model.vertexPositionsZ[point];
			if (modelVertexSkins == 1)
			{
				model.vertexSkins[point] = var6.readUnsignedByte();
			}
//			log.debug("x:{} y:{} z:{} f:{} dx:{} dy:{} dz:{} lx:{} ly:{} lz:{}", lastVertexX, lastVertexY, lastVertexZ,
//					vertexFlags, vertexXOffset, vertexYOffset, vertexZOffset, preX, preY, preZ);
		}

		var2.setOffset(colorPos);
		var24.setOffset(renderTypePos);
		var3.setOffset(priorityPos);
		var28.setOffset(alphaPos);
		var6.setOffset(triangleSkinPos);
		var55.setOffset(texturePos);
		var51.setOffset(textureCoordPos);

		for (point = 0; point < faceCount; ++point)
		{
			model.faceColors[point] = (short) var2.readUnsignedShort();
			if (hasFaceRenderTypes == 1)
			{
				model.faceRenderTypes[point] = var24.readByte();
			}

			if (modelPriority == 255)
			{
				model.faceRenderPriorities[point] = var3.readByte();
			}

			if (hasFaceAlphas == 1)
			{
				model.faceAlphas[point] = var28.readByte();
			}

			if (hasFaceSkins == 1)
			{
				model.faceSkins[point] = var6.readUnsignedByte();
			}

			if (modelTexture == 1)
			{
				model.faceTextures[point] = (short) (var55.readUnsignedShort() - 1);
			}

			if (model.textureCoordinates != null && model.faceTextures[point] != -1)
			{
				model.textureCoordinates[point] = (byte) (var51.readUnsignedByte() - 1);
			}
		}

		var2.setOffset(var11);
		var24.setOffset(var49);
		int index1 = 0;
		int index2 = 0;
		int index3 = 0;
		int lastIndex3 = 0;

//		log.debug("index1:{} index2:{} index3:{} lastIndex3:{}", index1, index2, index3, lastIndex3);
		for (int i = 0; i < faceCount; ++i)
		{
			int vertexIndexType = var24.readUnsignedByte();
			if (vertexIndexType == 1)
			{
				index1 = var2.readShortSmart() + lastIndex3;
				index2 = var2.readShortSmart() + index1;
				index3 = var2.readShortSmart() + index2;
				lastIndex3 = index3;
				model.faceVertexIndices1[i] = index1;
				model.faceVertexIndices2[i] = index2;
				model.faceVertexIndices3[i] = index3;
			}

			if (vertexIndexType == 2)
			{
				index2 = index3;
				index3 = var2.readShortSmart() + lastIndex3;
				lastIndex3 = index3;
				model.faceVertexIndices1[i] = index1;
				model.faceVertexIndices2[i] = index2;
				model.faceVertexIndices3[i] = index3;
			}

			if (vertexIndexType == 3)
			{
				index1 = index3;
				index3 = var2.readShortSmart() + lastIndex3;
				lastIndex3 = index3;
				model.faceVertexIndices1[i] = index1;
				model.faceVertexIndices2[i] = index2;
				model.faceVertexIndices3[i] = index3;
			}

			if (vertexIndexType == 4)
			{
				int temp = index1;
				index1 = index2;
				index2 = temp;
				index3 = var2.readShortSmart() + lastIndex3;
				lastIndex3 = index3;
				model.faceVertexIndices1[i] = index1;
				model.faceVertexIndices2[i] = temp;
				model.faceVertexIndices3[i] = index3;
			}

			log.debug("type: {} index1:{} index2:{} index3:{} lastIndex3:{}", vertexIndexType, index1, index2, index3, lastIndex3);
		}

		var2.setOffset(var43);
		var24.setOffset(var37);
		var3.setOffset(var48);
		var28.setOffset(var56);
		var6.setOffset(var45);
		var55.setOffset(var46);

		for (int texIndex = 0; texIndex < textureTriangleCount; ++texIndex)
		{
			int type = model.textureRenderTypes[texIndex] & 255;
			if (type == 0)
			{
				model.textureTriangleVertexIndices1[texIndex] = (short) var2.readUnsignedShort();
				model.textureTriangleVertexIndices2[texIndex] = (short) var2.readUnsignedShort();
				model.textureTriangleVertexIndices3[texIndex] = (short) var2.readUnsignedShort();
			}

			if (type == 1)
			{
				model.textureTriangleVertexIndices1[texIndex] = (short) var24.readUnsignedShort();
				model.textureTriangleVertexIndices2[texIndex] = (short) var24.readUnsignedShort();
				model.textureTriangleVertexIndices3[texIndex] = (short) var24.readUnsignedShort();
				model.aShortArray2574[texIndex] = (short) var3.readUnsignedShort();
				model.aShortArray2575[texIndex] = (short) var3.readUnsignedShort();
				model.aShortArray2586[texIndex] = (short) var3.readUnsignedShort();
				model.aShortArray2577[texIndex] = (short) var28.readUnsignedShort();
				model.aByteArray2580[texIndex] = var6.readByte();
				model.aShortArray2578[texIndex] = (short) var55.readUnsignedShort();
			}

			if (type == 2)
			{
				model.textureTriangleVertexIndices1[texIndex] = (short) var24.readUnsignedShort();
				model.textureTriangleVertexIndices2[texIndex] = (short) var24.readUnsignedShort();
				model.textureTriangleVertexIndices3[texIndex] = (short) var24.readUnsignedShort();
				model.aShortArray2574[texIndex] = (short) var3.readUnsignedShort();
				model.aShortArray2575[texIndex] = (short) var3.readUnsignedShort();
				model.aShortArray2586[texIndex] = (short) var3.readUnsignedShort();
				model.aShortArray2577[texIndex] = (short) var28.readUnsignedShort();
				model.aByteArray2580[texIndex] = var6.readByte();
				model.aShortArray2578[texIndex] = (short) var55.readUnsignedShort();
				model.texturePrimaryColors[texIndex] = (short) var55.readUnsignedShort();
			}

			if (type == 3)
			{
				model.textureTriangleVertexIndices1[texIndex] = (short) var24.readUnsignedShort();
				model.textureTriangleVertexIndices2[texIndex] = (short) var24.readUnsignedShort();
				model.textureTriangleVertexIndices3[texIndex] = (short) var24.readUnsignedShort();
				model.aShortArray2574[texIndex] = (short) var3.readUnsignedShort();
				model.aShortArray2575[texIndex] = (short) var3.readUnsignedShort();
				model.aShortArray2586[texIndex] = (short) var3.readUnsignedShort();
				model.aShortArray2577[texIndex] = (short) var28.readUnsignedShort();
				model.aByteArray2580[texIndex] = var6.readByte();
				model.aShortArray2578[texIndex] = (short) var55.readUnsignedShort();
			}
		}

		var2.setOffset(position);
		lastIndex3 = var2.readUnsignedByte();
		if (lastIndex3 != 0)
		{
			//new Class41();
			var2.readUnsignedShort();
			var2.readUnsignedShort();
			var2.readUnsignedShort();
			var2.readInt();
		}
	}

	private void load2(ModelDefinition model, byte[] var1)
	{
		boolean _hasFaceRenderTypes = false;
		boolean _hasFaceTextures = false;
		InputStream modelDataBuffer = new InputStream(var1);
		InputStream var39 = new InputStream(var1);
		InputStream var26 = new InputStream(var1);
		InputStream var9 = new InputStream(var1);
		InputStream var3 = new InputStream(var1);
		modelDataBuffer.setOffset(var1.length - 18);
		int vertexCount = modelDataBuffer.readUnsignedShort();
		int faceCount = modelDataBuffer.readUnsignedShort();
		int textureTriangleCount = modelDataBuffer.readUnsignedByte();
		int hasFaceRenderTypes = modelDataBuffer.readUnsignedByte();
		int hasFaceRenderPriorities = modelDataBuffer.readUnsignedByte();
		int hasFaceAlphas = modelDataBuffer.readUnsignedByte();
		int hasFaceSkins = modelDataBuffer.readUnsignedByte();
		int hasVertexSkins = modelDataBuffer.readUnsignedByte();
		int vertexXOffsets = modelDataBuffer.readUnsignedShort();
		int vertexYOffsets = modelDataBuffer.readUnsignedShort();
		modelDataBuffer.readUnsignedShort();
		int vertexIndexCount = modelDataBuffer.readUnsignedShort();
		byte bufferStart = 0;
		int offset = bufferStart + vertexCount;
		int offsetFaceData = offset;
		offset += faceCount;
		int offsetRenderPriorities = offset;
		if (hasFaceRenderPriorities == 255)
		{
			offset += faceCount;
		}

		int offsetFaceSkins = offset;
		if (hasFaceSkins == 1)
		{
			offset += faceCount;
		}

		int offsetRenderTypes = offset;
		if (hasFaceRenderTypes == 1)
		{
			offset += faceCount;
		}

		int offsetVertexSkins = offset;
		if (hasVertexSkins == 1)
		{
			offset += vertexCount;
		}

		int offsetFaceAlphas = offset;
		if (hasFaceAlphas == 1)
		{
			offset += faceCount;
		}

		int offsetVertexIndices = offset;
		offset += vertexIndexCount;
		int offsetFaceColor = offset;
		offset += faceCount * 2;
		int offsetTriangleTextures = offset;
		offset += textureTriangleCount * 6;
		int offsetVerticesX = offset;
		offset += vertexXOffsets;
		int offsetVerticesY = offset;
		offset += vertexYOffsets;
		model.vertexCount = vertexCount;
		model.faceCount = faceCount;
		model.textureTriangleCount = textureTriangleCount;
		model.vertexPositionsX = new int[vertexCount];
		model.vertexPositionsY = new int[vertexCount];
		model.vertexPositionsZ = new int[vertexCount];
		model.faceVertexIndices1 = new int[faceCount];
		model.faceVertexIndices2 = new int[faceCount];
		model.faceVertexIndices3 = new int[faceCount];
		if (textureTriangleCount > 0)
		{
			model.textureRenderTypes = new byte[textureTriangleCount];
			model.textureTriangleVertexIndices1 = new short[textureTriangleCount];
			model.textureTriangleVertexIndices2 = new short[textureTriangleCount];
			model.textureTriangleVertexIndices3 = new short[textureTriangleCount];
		}

		if (hasVertexSkins == 1)
		{
			model.vertexSkins = new int[vertexCount];
		}

		if (hasFaceRenderTypes == 1)
		{
			model.faceRenderTypes = new byte[faceCount];
			model.textureCoordinates = new byte[faceCount];
			model.faceTextures = new short[faceCount];
		}

		if (hasFaceRenderPriorities == 255)
		{
			model.faceRenderPriorities = new byte[faceCount];
		}
		else
		{
			model.priority = (byte) hasFaceRenderPriorities;
		}

		if (hasFaceAlphas == 1)
		{
			model.faceAlphas = new byte[faceCount];
		}

		if (hasFaceSkins == 1)
		{
			model.faceSkins = new int[faceCount];
		}

		model.faceColors = new short[faceCount];
		modelDataBuffer.setOffset(bufferStart);
		var39.setOffset(offsetVerticesX);
		var26.setOffset(offsetVerticesY);
		var9.setOffset(offset);
		var3.setOffset(offsetVertexSkins);
		int lastVertexX = 0;
		int lastVertexY = 0;
		int lastVertexZ = 0;

		for (int i = 0; i < vertexCount; ++i)
		{
			int vertexFlag = modelDataBuffer.readUnsignedByte();
			int vertexDeltaX = 0;
			if ((vertexFlag & 1) != 0)
			{
				vertexDeltaX = var39.readShortSmart();
			}

			int vertexDeltaY = 0;
			if ((vertexFlag & 2) != 0)
			{
				vertexDeltaY = var26.readShortSmart();
			}

			int vertexDeltaZ = 0;
			if ((vertexFlag & 4) != 0)
			{
				vertexDeltaZ = var9.readShortSmart();
			}

			model.vertexPositionsX[i] = lastVertexX + vertexDeltaX;
			model.vertexPositionsY[i] = lastVertexY + vertexDeltaY;
			model.vertexPositionsZ[i] = lastVertexZ + vertexDeltaZ;
			lastVertexX = model.vertexPositionsX[i];
			lastVertexY = model.vertexPositionsY[i];
			lastVertexZ = model.vertexPositionsZ[i];
			if (hasVertexSkins == 1)
			{
				model.vertexSkins[i] = var3.readUnsignedByte();
			}
		}

		modelDataBuffer.setOffset(offsetFaceColor);
		var39.setOffset(offsetRenderTypes);
		var26.setOffset(offsetRenderPriorities);
		var9.setOffset(offsetFaceAlphas);
		var3.setOffset(offsetFaceSkins);

		for (int i = 0; i < faceCount; ++i)
		{
			model.faceColors[i] = (short) modelDataBuffer.readUnsignedShort();
			if (hasFaceRenderTypes == 1)
			{
				int var8 = var39.readUnsignedByte();
				if ((var8 & 1) == 1)
				{
					model.faceRenderTypes[i] = 1;
					_hasFaceRenderTypes = true;
				}
				else
				{
					model.faceRenderTypes[i] = 0;
				}

				if ((var8 & 2) == 2)
				{
					model.textureCoordinates[i] = (byte) (var8 >> 2);
					model.faceTextures[i] = model.faceColors[i];
					model.faceColors[i] = 127;
					if (model.faceTextures[i] != -1)
					{
						_hasFaceTextures = true;
					}
				}
				else
				{
					model.textureCoordinates[i] = -1;
					model.faceTextures[i] = -1;
				}
			}

			if (hasFaceRenderPriorities == 255)
			{
				model.faceRenderPriorities[i] = var26.readByte();
			}

			if (hasFaceAlphas == 1)
			{
				model.faceAlphas[i] = var9.readByte();
			}

			if (hasFaceSkins == 1)
			{
				model.faceSkins[i] = var3.readUnsignedByte();
			}
		}

		modelDataBuffer.setOffset(offsetVertexIndices);
		var39.setOffset(offsetFaceData);
		int lastIndex1 = 0;
		int lastIndex2 = 0;
		int lastIndex3 = 0;
		int lastIndex = 0;

		int faceData;
		for (int i = 0; i < faceCount; ++i)
		{
			faceData = var39.readUnsignedByte();
			if (faceData == 1)
			{
				lastIndex1 = modelDataBuffer.readShortSmart() + lastIndex;
				lastIndex2 = modelDataBuffer.readShortSmart() + lastIndex1;
				lastIndex3 = modelDataBuffer.readShortSmart() + lastIndex2;
				lastIndex = lastIndex3;
				model.faceVertexIndices1[i] = lastIndex1;
				model.faceVertexIndices2[i] = lastIndex2;
				model.faceVertexIndices3[i] = lastIndex3;
			}

			if (faceData == 2)
			{
				lastIndex2 = lastIndex3;
				lastIndex3 = modelDataBuffer.readShortSmart() + lastIndex;
				lastIndex = lastIndex3;
				model.faceVertexIndices1[i] = lastIndex1;
				model.faceVertexIndices2[i] = lastIndex2;
				model.faceVertexIndices3[i] = lastIndex3;
			}

			if (faceData == 3)
			{
				lastIndex1 = lastIndex3;
				lastIndex3 = modelDataBuffer.readShortSmart() + lastIndex;
				lastIndex = lastIndex3;
				model.faceVertexIndices1[i] = lastIndex1;
				model.faceVertexIndices2[i] = lastIndex2;
				model.faceVertexIndices3[i] = lastIndex3;
			}

			if (faceData == 4)
			{
				int temp = lastIndex1;
				lastIndex1 = lastIndex2;
				lastIndex2 = temp;
				lastIndex3 = modelDataBuffer.readShortSmart() + lastIndex;
				lastIndex = lastIndex3;
				model.faceVertexIndices1[i] = lastIndex1;
				model.faceVertexIndices2[i] = lastIndex2;
				model.faceVertexIndices3[i] = lastIndex3;
			}
		}

		modelDataBuffer.setOffset(offsetTriangleTextures);

		for (int i = 0; i < textureTriangleCount; ++i)
		{
			model.textureRenderTypes[i] = 0;
			model.textureTriangleVertexIndices1[i] = (short) modelDataBuffer.readUnsignedShort();
			model.textureTriangleVertexIndices2[i] = (short) modelDataBuffer.readUnsignedShort();
			model.textureTriangleVertexIndices3[i] = (short) modelDataBuffer.readUnsignedShort();
		}

		if (model.textureCoordinates != null)
		{
			boolean var45 = false;

			for (faceData = 0; faceData < faceCount; ++faceData)
			{
				int var21 = model.textureCoordinates[faceData] & 255;
				if (var21 != 255)
				{
					if ((model.textureTriangleVertexIndices1[var21] & '\uffff') == model.faceVertexIndices1[faceData] && (model.textureTriangleVertexIndices2[var21] & '\uffff') == model.faceVertexIndices2[faceData] && (model.textureTriangleVertexIndices3[var21] & '\uffff') == model.faceVertexIndices3[faceData])
					{
						model.textureCoordinates[faceData] = -1;
					}
					else
					{
						var45 = true;
					}
				}
			}

			if (!var45)
			{
				model.textureCoordinates = null;
			}
		}

		if (!_hasFaceTextures)
		{
			model.faceTextures = null;
		}

		if (!_hasFaceRenderTypes)
		{
			model.faceRenderTypes = null;
		}
	}

}
