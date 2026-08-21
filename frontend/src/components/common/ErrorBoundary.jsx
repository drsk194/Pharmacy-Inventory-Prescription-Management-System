import { Component } from "react";

export default class ErrorBoundary extends Component {
  state = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    console.error("Uncaught render error:", error, info);
  }

  handleReload = () => {
    this.setState({ hasError: false });
    window.location.href = "/";
  };

  render() {
    if (!this.state.hasError) return this.props.children;
    return <main className="error-boundary"><h1>Something went wrong</h1><p>An unexpected error occurred. You can return to the home page.</p><button type="button" onClick={this.handleReload}>Return home</button></main>;
  }
}
