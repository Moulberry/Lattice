package com.moulberry.lattice.widget;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface Positionable {

    void setX(int x);
    void setY(int y);
    int getX();
    int getY();

    void setWidth(int width);
    void setHeight(int height);
    int getWidth();
    int getHeight();

}
