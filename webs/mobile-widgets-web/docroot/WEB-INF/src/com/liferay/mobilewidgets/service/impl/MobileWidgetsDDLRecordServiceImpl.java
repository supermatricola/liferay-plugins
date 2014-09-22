/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.mobilewidgets.service.impl;

import com.liferay.mobilewidgets.service.base.MobileWidgetsDDLRecordServiceBaseImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portlet.dynamicdatalists.model.DDLRecord;
import com.liferay.portlet.dynamicdatamapping.storage.Field;
import com.liferay.portlet.dynamicdatamapping.storage.FieldConstants;
import com.liferay.portlet.dynamicdatamapping.storage.Fields;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author José Manuel Navarro
 */
public class MobileWidgetsDDLRecordServiceImpl
	extends MobileWidgetsDDLRecordServiceBaseImpl {

	@Override
	public JSONObject getDDLRecord(long ddlRecordId, Locale locale)
		throws PortalException, SystemException {

		DDLRecord ddlRecord = ddlRecordPersistence.findByPrimaryKey(
			ddlRecordId);

		Map<String, Object> ddlRecordAttributes = new HashMap<String, Object>();

		Fields fields = ddlRecord.getFields();

		Set<Locale> availableLocales = fields.getAvailableLocales();

		if ((locale == null) || !availableLocales.contains(locale)) {
			locale = fields.getDefaultLocale();
		}

		for (Field field : fields) {
			Object fieldValue = getTypedFieldValue(field, locale);

			if (fieldValue != null) {
				ddlRecordAttributes.put(field.getName(), fieldValue);
			}
		}

		JSONObject ddlRecordJSONObject =
			JSONFactoryUtil.createJSONObject(
				JSONFactoryUtil.looseSerialize(ddlRecordAttributes));

		return ddlRecordJSONObject;
	}

	@Override
	public JSONArray getDDLRecords(
			long ddlRecordSetId, long userId, Locale locale, int start, int end)
		throws PortalException, SystemException {

		JSONArray ddlRecordsJSONArray = JSONFactoryUtil.createJSONArray();

		List<DDLRecord> ddlRecords = ddlRecordPersistence.findByR_U(
			ddlRecordSetId, userId, start, end);

		for (DDLRecord ddlRecord : ddlRecords) {
			JSONObject ddlRecordJSONObject = JSONFactoryUtil.createJSONObject();

			Map<String, Object> ddlRecordModelAttributes =
				ddlRecord.getModelAttributes();

			JSONObject ddlRecordModelAttributesJSONObject =
				JSONFactoryUtil.createJSONObject(
					JSONFactoryUtil.looseSerialize(ddlRecordModelAttributes));

			ddlRecordJSONObject.put(
				"modelAttributes", ddlRecordModelAttributesJSONObject);

			JSONObject ddlRecordValuesJSONObject = getDDLRecord(
				ddlRecord.getRecordId(), locale);

			ddlRecordJSONObject.put("modelValues", ddlRecordValuesJSONObject);

			ddlRecordsJSONArray.put(ddlRecordJSONObject);
		}

		return ddlRecordsJSONArray;
	}

	@Override
	public int getDDLRecordsCount(long ddlRecordSetId, long userId)
		throws SystemException {

		return ddlRecordPersistence.countByR_U(ddlRecordSetId, userId);
	}

	protected Object getTypedFieldValue(Field field, Locale locale)
		throws PortalException, SystemException {

		Object fieldValue;

		String fieldStringValue = String.valueOf(field.getValue(locale));

		String dataType = field.getDataType();

		if (fieldStringValue.equals("null")) {
			fieldValue = null;
		}
		else if (dataType.equals(FieldConstants.BOOLEAN)) {
			fieldValue = Boolean.valueOf(fieldStringValue);
		}
		else if (dataType.equals(FieldConstants.INTEGER)) {
			fieldValue = Integer.valueOf(fieldStringValue);
		}
		else if (dataType.equals(FieldConstants.LONG)) {
			fieldValue = Long.valueOf(fieldStringValue);
		}
		else if (dataType.equals(FieldConstants.SHORT)) {
			fieldValue = Short.valueOf(fieldStringValue);
		}
		else if (dataType.equals(FieldConstants.FLOAT) ||
				 dataType.equals(FieldConstants.NUMBER)) {

			fieldValue = Float.valueOf(fieldStringValue);
		}
		else if (dataType.equals(FieldConstants.DATE)) {
			fieldValue = field.getRenderedValue(locale);
		}
		else {
			fieldValue = fieldStringValue;
		}

		return fieldValue;
	}

}