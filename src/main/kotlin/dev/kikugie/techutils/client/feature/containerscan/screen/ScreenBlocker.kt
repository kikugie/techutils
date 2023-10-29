package dev.kikugie.techutils.client.feature.containerscan.screen

import dev.kikugie.techutils.client.task.ContiniousTask
import dev.kikugie.techutils.client.task.TaskManager
import net.minecraft.client.gui.screen.Screen

class ScreenBlocker(
    private val action: (Screen) -> ActionResult
) {
    var timeout = 100
    companion object : ContiniousTask() {
        private val queue = ArrayList<ScreenBlocker>()

        fun add(action: (Screen) -> ActionResult): ScreenBlocker {
            val handler = ScreenBlocker(action)
            queue.add(handler)
            return handler
        }

        fun register() {
            TaskManager.add(this)
        }
        fun onScreen(screen: Screen): Boolean {
            var close = false
            val itr = queue.iterator()
            while (itr.hasNext()) {
                val result = itr.next().action.invoke(screen)
                if (result != ActionResult.FAIL) itr.remove()
                close = result == ActionResult.BLOCK
            }
            return close
        }

        override fun tick() {
            val itr = queue.iterator()
            while (itr.hasNext()) {
                val handler = itr.next()
                handler.timeout--
                if (handler.timeout <= 0) itr.remove()
            }
        }

        override fun cancel() {
            queue.clear()
        }
    }

    enum class ActionResult{
        FAIL,
        PASS,
        BLOCK
    }
}