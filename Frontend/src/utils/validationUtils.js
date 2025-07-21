
export const validateRequired = (value, fieldName = 'Trường này') => {
    if (!value || typeof value !== 'string') {
        return {
            isValid: false,
            message: `${fieldName} không được để trống`,
        }
    }

    if (value.trim() === '') {
        return {
            isValid: false,
            message: `${fieldName} không được để trống`,
        }
    }

    return {
        isValid: true,
        message: '',
    }
}

export const validateMultipleRequired = (fields, fieldLabels = {}) => {
    const invalidFields = []

    for (const [fieldName, value] of Object.entries(fields)) {
        const displayName = fieldLabels[fieldName] || fieldName
        const validation = validateRequired(value, displayName)

        if (!validation.isValid) {
            invalidFields.push({
                field: fieldName,
                message: validation.message,
            })
        }
    }

    return {
        isValid: invalidFields.length === 0,
        message: invalidFields.length > 0 ? invalidFields[0].message : '',
        invalidFields,
    }
}


export const isOnlyWhitespace = (value) => {
    return typeof value === 'string' && value.trim() === '' && value.length > 0
}


export const isOnlyNumbers = (value) => {
    return typeof value === 'string' && value.trim() !== '' && /^\d+$/.test(value.trim())
}


export const validateNotOnlyNumbers = (value, fieldName = 'Trường này', allowOnlyNumbers = false) => {
    if (!value || typeof value !== 'string') {
        return {
            isValid: false,
            message: `${fieldName} không được để trống`,
        }
    }

    if (value.trim() === '') {
        return {
            isValid: false,
            message: `${fieldName} không được để trống`,
        }
    }

    if (!allowOnlyNumbers && isOnlyNumbers(value)) {
        return {
            isValid: false,
            message: `${fieldName} không được chỉ chứa số`,
        }
    }

    return {
        isValid: true,
        message: '',
    }
}

export const trimValue = (value) => {
    return typeof value === 'string' ? value.trim() : value
}

export const validateFormSubmission = (formData, requiredFields) => {
    const fieldsToValidate = {}
    const fieldLabels = {}
    const fieldOptions = {}

    requiredFields.forEach((field) => {
        if (typeof field === 'string') {
            fieldsToValidate[field] = formData[field]
            fieldLabels[field] = field
            fieldOptions[field] = { allowOnlyNumbers: false }
        } else if (typeof field === 'object') {
            fieldsToValidate[field.key] = formData[field.key]
            fieldLabels[field.key] = field.label || field.key
            fieldOptions[field.key] = {
                allowOnlyNumbers: field.allowOnlyNumbers || false,
                skipNumberValidation: field.skipNumberValidation || false
            }
        }
    })

    const requiredValidation = validateMultipleRequired(fieldsToValidate, fieldLabels)
    if (!requiredValidation.isValid) {
        return requiredValidation
    }

    const invalidFields = []
    for (const [fieldName, value] of Object.entries(fieldsToValidate)) {
        const options = fieldOptions[fieldName]
        const displayName = fieldLabels[fieldName]

        if (!options.skipNumberValidation) {
            const numberValidation = validateNotOnlyNumbers(value, displayName, options.allowOnlyNumbers)
            if (!numberValidation.isValid) {
                invalidFields.push({
                    field: fieldName,
                    message: numberValidation.message,
                })
            }
        }
    }

    return {
        isValid: invalidFields.length === 0,
        message: invalidFields.length > 0 ? invalidFields[0].message : '',
        invalidFields,
    }
}

export default {
    validateRequired,
    validateMultipleRequired,
    validateNotOnlyNumbers,
    isOnlyWhitespace,
    isOnlyNumbers,
    trimValue,
    validateFormSubmission,
}
