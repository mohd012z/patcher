package com.msa.patcher.modify.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class PerViewZoomControllerTest {

    @Test
    fun newViewsStartAtOneHundredPercent() {
        val controller = PerViewZoomController()
        assertEquals(100, controller.percent(ZoomViewKey.EDITOR))
        assertEquals(100, controller.percent(ZoomViewKey.AI))
    }

    @Test
    fun scaleIsRememberedIndependentlyPerView() {
        val controller = PerViewZoomController()
        controller.setPercent(ZoomViewKey.EDITOR, 150)
        controller.setPercent(ZoomViewKey.SEARCH, 80)

        assertEquals(150, controller.percent(ZoomViewKey.EDITOR))
        assertEquals(80, controller.percent(ZoomViewKey.SEARCH))
        assertEquals(100, controller.percent(ZoomViewKey.DIFF))
    }

    @Test
    fun zoomIsClampedBetweenFiftyAndThreeHundredPercent() {
        val controller = PerViewZoomController()
        assertEquals(50, controller.setPercent(ZoomViewKey.EDITOR, 1))
        assertEquals(300, controller.setPercent(ZoomViewKey.EDITOR, 999))
    }

    @Test
    fun scaleFactorUsesCurrentViewValueAndClamps() {
        val controller = PerViewZoomController()
        controller.setPercent(ZoomViewKey.EDITOR, 125)

        assertEquals(150, controller.scale(ZoomViewKey.EDITOR, 1.2f))
        assertEquals(300, controller.scale(ZoomViewKey.EDITOR, 10f))
    }

    @Test
    fun resetAffectsOnlyRequestedView() {
        val controller = PerViewZoomController()
        controller.setPercent(ZoomViewKey.EDITOR, 175)
        controller.setPercent(ZoomViewKey.AI, 140)

        assertEquals(100, controller.reset(ZoomViewKey.EDITOR))
        assertEquals(140, controller.percent(ZoomViewKey.AI))
    }
}
