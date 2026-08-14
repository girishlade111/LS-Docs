package com.example.data.util

import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

sealed class DataNode(val key: String, val path: String) {
    data class ValueNode(
        val nodeKey: String,
        val value: Any?,
        val valueType: String,
        val nodePath: String
    ) : DataNode(nodeKey, nodePath)

    data class ObjectNode(
        val nodeKey: String,
        val children: List<DataNode>,
        val nodePath: String
    ) : DataNode(nodeKey, nodePath)

    data class ArrayNode(
        val nodeKey: String,
        val children: List<DataNode>,
        val nodePath: String
    ) : DataNode(nodeKey, nodePath)
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val line: Int? = null,
    val column: Int? = null,
    val keyCount: Int = 0,
    val maxNestingDepth: Int = 0
)

object StructuredDataParser {

    fun parseJsonToTree(jsonStr: String): DataNode? {
        val trimmed = jsonStr.trim()
        return try {
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                parseJsonObject("root", obj, "$")
            } else if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                parseJsonArray("root", arr, "$")
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseJsonObject(key: String, obj: JSONObject, path: String): DataNode.ObjectNode {
        val children = mutableListOf<DataNode>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val currentPath = "$path.$k"
            val value = obj.get(k)
            when (value) {
                is JSONObject -> children.add(parseJsonObject(k, value, currentPath))
                is JSONArray -> children.add(parseJsonArray(k, value, currentPath))
                JSONObject.NULL -> children.add(DataNode.ValueNode(k, "null", "null", currentPath))
                else -> children.add(DataNode.ValueNode(k, value, value.javaClass.simpleName, currentPath))
            }
        }
        return DataNode.ObjectNode(key, children, path)
    }

    private fun parseJsonArray(key: String, arr: JSONArray, path: String): DataNode.ArrayNode {
        val children = mutableListOf<DataNode>()
        for (i in 0 until arr.length()) {
            val currentPath = "$path[$i]"
            val value = arr.get(i)
            when (value) {
                is JSONObject -> children.add(parseJsonObject("[$i]", value, currentPath))
                is JSONArray -> children.add(parseJsonArray("[$i]", value, currentPath))
                JSONObject.NULL -> children.add(DataNode.ValueNode("[$i]", "null", "null", currentPath))
                else -> children.add(DataNode.ValueNode("[$i]", value, value.javaClass.simpleName, currentPath))
            }
        }
        return DataNode.ArrayNode(key, children, path)
    }

    fun parseXmlToTree(xmlStr: String): DataNode? {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(ByteArrayInputStream(xmlStr.toByteArray()))
            doc.documentElement.normalize()
            parseXmlElement(doc.documentElement, "/")
        } catch (e: Exception) {
            null
        }
    }

    private fun parseXmlElement(element: Element, path: String): DataNode.ObjectNode {
        val nodeName = element.tagName
        val currentPath = "$path/$nodeName"
        val children = mutableListOf<DataNode>()

        // Attributes
        val attrs = element.attributes
        for (i in 0 until attrs.length) {
            val attr = attrs.item(i)
            children.add(DataNode.ValueNode("@${attr.nodeName}", attr.nodeValue, "attribute", "$currentPath/@${attr.nodeName}"))
        }

        // Child nodes
        val childNodes = element.childNodes
        for (i in 0 until childNodes.length) {
            val child = childNodes.item(i)
            if (child.nodeType == Node.ELEMENT_NODE) {
                children.add(parseXmlElement(child as Element, currentPath))
            } else if (child.nodeType == Node.TEXT_NODE && child.nodeValue.trim().isNotEmpty()) {
                children.add(DataNode.ValueNode("#text", child.nodeValue.trim(), "text", "$currentPath/#text"))
            }
        }

        return DataNode.ObjectNode(nodeName, children, currentPath)
    }

    fun validateJson(jsonStr: String): ValidationResult {
        val trimmed = jsonStr.trim()
        if (trimmed.isEmpty()) return ValidationResult(false, "File is empty")
        return try {
            if (trimmed.startsWith("{")) {
                JSONObject(trimmed)
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                return ValidationResult(false, "Root element must be Object or Array")
            }
            ValidationResult(true, null)
        } catch (e: Exception) {
            ValidationResult(false, e.message ?: "Invalid JSON syntax")
        }
    }

    fun formatJson(jsonStr: String, indentSpaces: Int = 2): String {
        val trimmed = jsonStr.trim()
        return try {
            if (trimmed.startsWith("{")) JSONObject(trimmed).toString(indentSpaces)
            else if (trimmed.startsWith("[")) JSONArray(trimmed).toString(indentSpaces)
            else jsonStr
        } catch (e: Exception) {
            jsonStr
        }
    }

    fun minifyJson(jsonStr: String): String {
        val trimmed = jsonStr.trim()
        return try {
            if (trimmed.startsWith("{")) JSONObject(trimmed).toString()
            else if (trimmed.startsWith("[")) JSONArray(trimmed).toString()
            else jsonStr
        } catch (e: Exception) {
            jsonStr
        }
    }
}
