package com.swent.suddenbump.model.direction

data class DirectionsResponse(val routes: List<Route>)

data class Route(val overview_polyline: OverviewPolyline)

data class OverviewPolyline(val points: String)
