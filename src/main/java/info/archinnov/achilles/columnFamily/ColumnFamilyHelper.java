package info.archinnov.achilles.columnFamily;

import static info.archinnov.achilles.serializer.SerializerUtils.OBJECT_SRZ;
import static info.archinnov.achilles.serializer.SerializerUtils.STRING_SRZ;
import info.archinnov.achilles.entity.PropertyHelper;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.exception.InvalidColumnFamilyException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.prettyprint.cassandra.serializers.SerializerTypeInferer;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ColumnFamilyBuilder
 * 
 * @author DuyHai DOAN
 * 
 */
public class ColumnFamilyHelper
{

	private static final Logger log = LoggerFactory.getLogger(ColumnFamilyHelper.class);

	private static final String DYNAMIC_TYPE_ALIASES = "(a=>AsciiType,b=>BytesType,c=>BooleanType,d=>DateType,e=>DecimalType,z=>DoubleType,f=>FloatType,i=>IntegerType,j=>Int32Type,x=>LexicalUUIDType,l=>LongType,t=>TimeUUIDType,s=>UTF8Type,u=>UUIDType)";
	private String COMPARATOR_TYPE_AND_ALIAS = "DynamicCompositeType(f=>org.apache.cassandra.db.marshal.FloatType,d=>org.apache.cassandra.db.marshal.DateType,e=>org.apache.cassandra.db.marshal.DecimalType,b=>org.apache.cassandra.db.marshal.BytesType,c=>org.apache.cassandra.db.marshal.BooleanType,a=>org.apache.cassandra.db.marshal.AsciiType,l=>org.apache.cassandra.db.marshal.LongType,j=>org.apache.cassandra.db.marshal.Int32Type,i=>org.apache.cassandra.db.marshal.IntegerType,u=>org.apache.cassandra.db.marshal.UUIDType,t=>org.apache.cassandra.db.marshal.TimeUUIDType,s=>org.apache.cassandra.db.marshal.UTF8Type,z=>org.apache.cassandra.db.marshal.DoubleType,x=>org.apache.cassandra.db.marshal.LexicalUUIDType)";

	public static final Pattern CF_PATTERN = Pattern.compile("[a-zA-Z0-9_]{1,48}");
	public PropertyHelper helper = new PropertyHelper();

	public <ID> ColumnFamilyDefinition buildDynamicCompositeCF(EntityMeta<ID> entityMeta,
			String keyspaceName)
	{

		String entityName = entityMeta.getClassName();
		String columnFamilyName = entityMeta.getColumnFamilyName();

		ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(keyspaceName,
				columnFamilyName, ComparatorType.DYNAMICCOMPOSITETYPE);

		String keyValidationType = entityMeta.getIdSerializer().getComparatorType().getTypeName();

		cfDef.setKeyValidationClass(keyValidationType);
		cfDef.setComparatorTypeAlias(DYNAMIC_TYPE_ALIASES);
		cfDef.setDefaultValidationClass(STRING_SRZ.getComparatorType().getTypeName());
		cfDef.setComment("Column family for entity '" + entityName + "'");

		StringBuilder builder = new StringBuilder("\n\n");
		builder.append("Create Dynamic Composite-based column family for entity ");
		builder.append("'").append(entityName).append("' : \n");
		builder.append("\tcreate column family ").append(columnFamilyName).append("\n");
		builder.append("\t\twith key_validation_class = ").append(keyValidationType).append("\n");
		builder.append("\t\tand comparator = '").append(
				ComparatorType.DYNAMICCOMPOSITETYPE.getTypeName());
		builder.append(DYNAMIC_TYPE_ALIASES).append("'\n");
		builder.append("\t\tand default_validation_class = ")
				.append(ComparatorType.UTF8TYPE.getTypeName()).append("\n");
		builder.append("\t\tand comment = 'Column family for entity ").append(entityName)
				.append("'\n\n");

		log.debug(builder.toString());

		return cfDef;
	}

	public <ID> ColumnFamilyDefinition buildCompositeCF(String keyspaceName,
			PropertyMeta<?, ?> propertyMeta, Class<ID> keyClass, String columnFamilyName,
			String entityName)
	{
		Class<?> valueClass = propertyMeta.getValueClass();

		Serializer<?> keySerializer = SerializerTypeInferer.getSerializer(keyClass);
		ComparatorType comparatorType = ComparatorType.COMPOSITETYPE;
		String comparatorTypesAlias = helper.determineCompatatorTypeAliasForCompositeCF(
				propertyMeta, true);

		ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(keyspaceName,
				columnFamilyName, comparatorType);

		String keyValidationType = keySerializer.getComparatorType().getTypeName();
		cfDef.setKeyValidationClass(keyValidationType);
		cfDef.setComparatorTypeAlias(comparatorTypesAlias);

		Serializer<?> valueSerializer = SerializerTypeInferer.getSerializer(valueClass);

		if (valueSerializer == OBJECT_SRZ)
		{
			if (propertyMeta.isJoin())
			{
				valueSerializer = propertyMeta.joinIdMeta().getValueSerializer();
			}
			else
			{
				valueSerializer = STRING_SRZ;
			}
		}

		String defaultValidationType = valueSerializer.getComparatorType().getTypeName();
		cfDef.setDefaultValidationClass(defaultValidationType);
		cfDef.setComment("Column family for entity '" + columnFamilyName + "'");

		String propertyName = propertyMeta.getPropertyName();

		StringBuilder builder = new StringBuilder("\n\n");
		builder.append("Create Composite-based column family for property ");
		builder.append("'").append(propertyName).append("' of entity '");
		builder.append(entityName).append("' : \n");
		builder.append("\tcreate column family ").append(columnFamilyName).append("\n");
		builder.append("\t\twith key_validation_class = ").append(keyValidationType).append("\n");
		builder.append("\t\tand comparator = '").append(ComparatorType.COMPOSITETYPE.getTypeName());
		builder.append(comparatorTypesAlias).append("'\n");
		builder.append("\t\tand default_validation_class = ").append(defaultValidationType)
				.append("\n");
		builder.append("\t\tand comment = 'Column family for property ").append(propertyName);
		builder.append(" of entity ").append(entityName).append("'\n\n");

		log.debug(builder.toString());

		return cfDef;
	}

	public void validateCFWithEntityMeta(ColumnFamilyDefinition cfDef, EntityMeta<?> entityMeta)
	{

		log.trace("Validating column family row key definition for entityMeta {}",
				entityMeta.getClassName());

		if (!StringUtils.equals(cfDef.getKeyValidationClass(), entityMeta.getIdSerializer()
				.getComparatorType().getClassName()))
		{
			throw new InvalidColumnFamilyException("The column family '"
					+ entityMeta.getColumnFamilyName() + "' key class '"
					+ cfDef.getKeyValidationClass()
					+ "' does not correspond to the entity id class '"
					+ entityMeta.getIdSerializer().getComparatorType().getClassName() + "'");
		}

		if (entityMeta.isColumnFamilyDirectMapping())
		{
			this.validateCFWithPropertyMeta(cfDef, entityMeta.getPropertyMetas().values()
					.iterator().next(), entityMeta.getColumnFamilyName());
		}
		else
		{

			log.trace(
					"Validating column family dynamic composite comparator definition for entityMeta {}",
					entityMeta.getClassName());

			if (cfDef.getComparatorType() == null
					|| !StringUtils.equals(cfDef.getComparatorType().getTypeName(),
							COMPARATOR_TYPE_AND_ALIAS))
			{
				throw new InvalidColumnFamilyException("The column family '"
						+ entityMeta.getColumnFamilyName() + "' comparator type should be '"
						+ COMPARATOR_TYPE_AND_ALIAS + "'");
			}
		}
	}

	public void validateCFWithPropertyMeta(ColumnFamilyDefinition cfDef,
			PropertyMeta<?, ?> propertyMeta, String externalColumnFamilyName)
	{
		log.trace("Validating column family composite comparator definition for propertyMeta {}",
				propertyMeta.getPropertyName());

		String comparatorTypeAlias = helper.determineCompatatorTypeAliasForCompositeCF(
				propertyMeta, false);
		if (cfDef.getComparatorType() == null
				|| !StringUtils
						.equals(cfDef.getComparatorType().getTypeName(), comparatorTypeAlias))
		{
			throw new InvalidColumnFamilyException("The column family '" + externalColumnFamilyName
					+ "' comparator type should be '" + comparatorTypeAlias + "'");
		}
	}

	public static String normalizerAndValidateColumnFamilyName(String cfName)
	{
		log.trace("Normalizing column family '{}' name agains Cassandra restrictions", cfName);

		Matcher nameMatcher = CF_PATTERN.matcher(cfName);

		if (nameMatcher.matches())
		{
			return cfName;
		}
		else if (cfName.contains("."))
		{
			String className = cfName.replaceAll(".+\\.(.+)", "$1");
			return normalizerAndValidateColumnFamilyName(className);
		}
		else
		{
			throw new InvalidColumnFamilyException(
					"The column family name '"
							+ cfName
							+ "' is invalid. It should be respect the pattern [a-zA-Z0-9_] and be at most 48 characters long");
		}
	}
}
