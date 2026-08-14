package com.example.data.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient

object PrintHelper {

    fun printDocument(context: Context, documentName: String, content: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "LSDocs_$documentName"

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }

        val escapedContent = content
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        val formattedHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>$documentName</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        padding: 24px;
                        color: #1a1a1a;
                        line-height: 1.6;
                        font-size: 14px;
                    }
                    h1 { color: #0f172a; font-size: 20px; border-bottom: 2px solid #e2e8f0; padding-bottom: 8px; }
                    pre {
                        font-family: "Courier New", Courier, monospace;
                        background: #f8fafc;
                        padding: 12px;
                        border: 1px solid #e2e8f0;
                        border-radius: 6px;
                        white-space: pre-wrap;
                        word-break: break-all;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <h1>$documentName</h1>
                <pre>$escapedContent</pre>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, formattedHtml, "text/html", "UTF-8", null)
    }
}
