package org.lesionscan.project.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lesionscan.project.domain.entities.LesionClassification
import org.lesionscan.project.domain.entities.RiskLevel

/**
 * Result screen displays ML classification results.
 * Shows: risk level, risk score, confidence, inference time.
 */
@Composable
fun ResultScreen(
    classification: LesionClassification,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.DarkGray, shape = RoundedCornerShape(12.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Analysis Complete",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Risk Level Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = getRiskColor(classification.riskLevel)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Risk Level",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        classification.riskLevel.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Risk Score
            Text(
                "Risk Score: ${String.format("%.2f", classification.riskScore)} / 1.0",
                fontSize = 16.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Confidence
            Text(
                "Confidence: ${String.format("%.0f", classification.confidenceScore * 100)}%",
                fontSize = 14.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Inference Time
            Text(
                "Inference Time: ${classification.inferenceTimeMs}ms",
                fontSize = 12.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Medical Disclaimer
            Text(
                "⚠ This app provides preliminary assessment only. Consult a dermatologist for diagnosis.",
                fontSize = 11.sp,
                color = Color.Yellow,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Retry Button
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan Another", fontSize = 14.sp)
            }
        }
    }
}

private fun getRiskColor(riskLevel: RiskLevel): Color {
    return when (riskLevel) {
        RiskLevel.LOW -> Color(0xFF4CAF50)      // Green
        RiskLevel.MEDIUM -> Color(0xFFFFC107)   // Amber
        RiskLevel.HIGH -> Color(0xFFF44336)     // Red
    }
}