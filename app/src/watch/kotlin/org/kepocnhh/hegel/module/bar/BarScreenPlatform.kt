package org.kepocnhh.hegel.module.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.BasicSwipeToDismissBox
import org.kepocnhh.hegel.util.compose.ListPlatform
import java.util.UUID

@Composable
internal fun BarScreen(
    onBack: () -> Unit,
    state: BarLogics.State,
    items: BarLogics.Items,
    onDelete: (UUID) -> Unit,
    onAdd: () -> Unit,
    onUpdate: (UUID) -> Unit,
) {
    BasicSwipeToDismissBox(
        modifier = Modifier.fillMaxSize(),
        onDismissed = onBack,
    ) { isBackground: Boolean ->
        if (!isBackground) Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            ListPlatform(
                modifier = Modifier.fillMaxSize(),
            ) {
                items.list.forEachIndexed { index, described ->
                    item(
                        key = described.id,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !state.loading) {
                                    onUpdate(described.id)
                                }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f),
                            ) {
                                BasicText(
                                    text = "$index) ${described.id}",
                                    style = TextStyle(fontSize = 10.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                BasicText(
                                    text = described.item.count.toString(),
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 14.sp,
                                    ),
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicText(
                                modifier = Modifier
                                    .background(Color.Black)
                                    .size(24.dp)
                                    .clickable(enabled = !state.loading) {
                                        onDelete(described.id)
                                    }
                                    .wrapContentSize(),
                                text = "x",
                                style = TextStyle(color = Color.White),
                            )
                        }
                    }
                }
            }
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = !state.loading, onClick = onAdd)
                    .wrapContentSize(),
                text = "+",
                style = TextStyle(color = Color.White),
            )
        }
    }
}
