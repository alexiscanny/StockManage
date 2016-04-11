/*
	Copyright 2013 Alessio Scannicchio (alessio.scannicchio@gmail.com).

 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at

  	http://www.apache.org/licenses/LICENSE-2.0

 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
*/

package com.example.tsa;

public class StockData {
	private String symbol;
	private double price;
	private double percentileChange;
	private String name;
	private double maximum;
	private double minimum;
	
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public void setPrice(Double price) { 
		this.price = price;
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPercentileChange(double percentileChange) {
		this.percentileChange = percentileChange;
	}
	
	public double getPercentileChange() {
		return percentileChange;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
	
	public double getMinimum() {
		return minimum;
	}
	
	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}
	
	public double getMaximum() {
		return maximum;
	}
}